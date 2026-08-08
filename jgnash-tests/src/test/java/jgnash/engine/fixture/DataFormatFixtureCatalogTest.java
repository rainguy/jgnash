/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2026 Craig Cavanaugh and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package jgnash.engine.fixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import jgnash.engine.DataStoreType;
import jgnash.engine.Engine;
import jgnash.engine.EngineFactory;
import jgnash.util.FileMagic;

/** Regression tests for the checked-in synthetic persistence catalog. */
class DataFormatFixtureCatalogTest {

    private static final Path CATALOG_ROOT = Path.of("src/test/resources/jgnash/engine/fixtures");

    @TempDir
    Path temporaryRoot;

    @TestFactory
    Stream<DynamicTest> validFixturesMatchReviewedSummaries() throws IOException {
        final Properties catalog = load(CATALOG_ROOT.resolve("catalog.properties"));
        return fixtureIds(catalog).stream().filter(id -> "valid".equals(value(catalog, id, "kind")))
                .map(id -> DynamicTest.dynamicTest(id, () -> verifyValidFixture(catalog, id)));
    }

    @TestFactory
    Stream<DynamicTest> corruptFixturesRemainTruncatedAndNonCanonical() throws IOException {
        final Properties catalog = load(CATALOG_ROOT.resolve("catalog.properties"));
        return fixtureIds(catalog).stream().filter(id -> "invalid".equals(value(catalog, id, "kind")))
                .map(id -> DynamicTest.dynamicTest(id, () -> {
                    final Path fixture = CATALOG_ROOT.resolve(value(catalog, id, "path"));
                    assertTrue(Files.size(fixture) <= 64, "Corrupt fixture must remain deliberately truncated");
                    assertEquals(value(catalog, id, "expectedFileMagic"), FileMagic.magic(fixture).name());
                }));
    }

    @TestFactory
    Stream<DynamicTest> passwordFixturesRejectAnIncorrectPassword() throws IOException {
        final Properties catalog = load(CATALOG_ROOT.resolve("catalog.properties"));
        return fixtureIds(catalog).stream().filter(id -> Boolean.parseBoolean(value(catalog, id,
                "passwordProtected"))).map(id -> DynamicTest.dynamicTest(id, () -> {
                    final Path sourceFile = CATALOG_ROOT.resolve(value(catalog, id, "path"));
                    final Path fixtureDirectory = temporaryRoot.resolve(id + "-wrong-password");
                    DataFormatFixtureSummaryPrinter.copyTree(sourceFile.getParent(), fixtureDirectory);
                    final Path copiedFile = fixtureDirectory.resolve(sourceFile.getFileName());
                    final Engine engine = EngineFactory.bootLocalEngine(copiedFile.toString(), EngineFactory.DEFAULT,
                            "incorrect-fixture-password".toCharArray());
                    if (engine != null) {
                        EngineFactory.closeEngine(EngineFactory.DEFAULT);
                    }
                    assertNull(engine, "Password-protected fixture accepted an incorrect password");
                }));
    }

    @Test
    void catalogCoversEveryStoreAndRequiredScenario() throws IOException {
        final Properties catalog = load(CATALOG_ROOT.resolve("catalog.properties"));
        final List<String> ids = fixtureIds(catalog);
        for (final DataStoreType type : DataStoreType.values()) {
            assertTrue(ids.stream().anyMatch(id -> type.name().equals(value(catalog, id, "dataStoreType"))),
                    "Missing fixture for " + type);
        }
        for (final String feature : List.of("attachments", "budgets", "corrupt", "empty", "investments",
                "minimal", "multiple-currencies", "password", "reconciled", "reminders", "splits", "tags")) {
            assertTrue(ids.stream().anyMatch(id -> Arrays.asList(value(catalog, id, "features").split(","))
                    .contains(feature)), "Missing scenario " + feature);
        }
        assertEquals(3, ids.stream().filter(id -> Boolean.parseBoolean(value(catalog, id, "passwordProtected")))
                .count(), "Only the SQL stores support password protection");
    }

    @Test
    void payloadChecksumsMatchReviewedManifest() throws Exception {
        final Properties checksums = load(CATALOG_ROOT.resolve("payload-sha256.properties"));
        assertFalse(checksums.isEmpty());

        final Set<String> payloadFiles = new TreeSet<>();
        try (Stream<Path> paths = Files.walk(CATALOG_ROOT)) {
            paths.filter(Files::isRegularFile).map(CATALOG_ROOT::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .filter(path -> !path.equals(".gitattributes"))
                    .filter(path -> !path.equals("README.md"))
                    .filter(path -> !path.equals("catalog.properties"))
                    .filter(path -> !path.equals("payload-sha256.properties"))
                    .filter(path -> !path.startsWith("expected/"))
                    .forEach(payloadFiles::add);
        }
        assertEquals(payloadFiles, new TreeSet<>(checksums.stringPropertyNames()),
                "Checksum manifest must cover every generated payload");

        for (final String relativePath : checksums.stringPropertyNames()) {
            final byte[] content = Files.readAllBytes(CATALOG_ROOT.resolve(relativePath));
            final String actual = toHex(MessageDigest.getInstance("SHA-256").digest(content));
            assertEquals(checksums.getProperty(relativePath), actual, relativePath);
        }
    }

    private void verifyValidFixture(final Properties catalog, final String id) throws Exception {
        final Path sourceFile = CATALOG_ROOT.resolve(value(catalog, id, "path"));
        final Path fixtureDirectory = temporaryRoot.resolve(id);
        DataFormatFixtureSummaryPrinter.copyTree(sourceFile.getParent(), fixtureDirectory);
        final Path copiedFile = fixtureDirectory.resolve(sourceFile.getFileName());
        final char[] password = value(catalog, id, "password").toCharArray();
        final String formatVersion = String.format(Locale.ROOT, "%.1f",
                EngineFactory.getFileVersion(copiedFile, password));

        final Engine engine = EngineFactory.bootLocalEngine(copiedFile.toString(), EngineFactory.DEFAULT, password);
        assertNotNull(engine, "Legacy-compatible reader could not open " + id);
        try {
            final Map<String, String> expected = new TreeMap<>();
            final Properties expectedProperties = load(CATALOG_ROOT.resolve(value(catalog, id, "expected")));
            for (final String key : expectedProperties.stringPropertyNames()) {
                expected.put(key, expectedProperties.getProperty(key));
            }
            assertEquals(expected, FixtureSummary.summarize(engine, copiedFile, formatVersion));
        } finally {
            EngineFactory.closeEngine(EngineFactory.DEFAULT);
        }
    }

    private static List<String> fixtureIds(final Properties catalog) {
        return new ArrayList<>(Arrays.asList(catalog.getProperty("fixture.ids").split(",")));
    }

    private static Properties load(final Path path) throws IOException {
        final Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        return properties;
    }

    private static String value(final Properties catalog, final String id, final String field) {
        return catalog.getProperty("fixture." + id + "." + field, "");
    }

    private static String toHex(final byte[] bytes) {
        final StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (final byte value : bytes) {
            builder.append(String.format("%02x", value & 0xff));
        }
        return builder.toString();
    }
}
