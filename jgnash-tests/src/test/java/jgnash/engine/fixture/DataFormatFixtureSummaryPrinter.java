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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import jgnash.engine.Engine;
import jgnash.engine.EngineFactory;

/** Prints summaries from copied fixtures for explicit human review. */
public final class DataFormatFixtureSummaryPrinter {

    private DataFormatFixtureSummaryPrinter() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the fixture catalog directory");
        }
        Locale.setDefault(Locale.US);
        final Path catalogRoot = Path.of(args[0]).toAbsolutePath().normalize();
        final Properties catalog = load(catalogRoot.resolve("catalog.properties"));
        final List<String> ids = Arrays.asList(catalog.getProperty("fixture.ids").split(","));

        for (final String id : ids) {
            if (!"valid".equals(catalog.getProperty("fixture." + id + ".kind"))) {
                continue;
            }
            final Path sourceFile = catalogRoot.resolve(catalog.getProperty("fixture." + id + ".path"));
            final Path temporaryDirectory = Files.createTempDirectory("jgnash-fixture-summary-");
            try {
                copyTree(sourceFile.getParent(), temporaryDirectory);
                final Path copiedFile = temporaryDirectory.resolve(sourceFile.getFileName());
                final char[] password = catalog.getProperty("fixture." + id + ".password", "").toCharArray();
                final String formatVersion = String.format(Locale.ROOT, "%.1f",
                        EngineFactory.getFileVersion(copiedFile, password));
                final Engine engine = EngineFactory.bootLocalEngine(copiedFile.toString(), EngineFactory.DEFAULT,
                        password);
                if (engine == null) {
                    throw new IllegalStateException("Could not open " + id);
                }
                try {
                    System.out.println("=== " + id + " ===");
                    System.out.print(FixtureSummary.asProperties(FixtureSummary.summarize(engine, copiedFile,
                            formatVersion)));
                } finally {
                    EngineFactory.closeEngine(EngineFactory.DEFAULT);
                }
            } finally {
                deleteTree(temporaryDirectory);
            }
        }
    }

    static Properties load(final Path path) throws IOException {
        final Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        return properties;
    }

    static void copyTree(final Path source, final Path destination) throws IOException {
        try (var paths = Files.walk(source)) {
            for (final Path path : paths.collect(Collectors.toList())) {
                final Path target = destination.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    static void deleteTree(final Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            for (final Path path : paths.sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.deleteIfExists(path);
            }
        }
    }
}
