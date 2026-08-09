/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2026 The jGnash contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package jgnash.bootloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BootLoaderTest {

    @Test
    void usesCatalogJavaFxVersionForPlatformDownloads() {
        final String expectedVersion = System.getProperty("jgnash.test.javafx.version");

        assertEquals(expectedVersion, BootLoader.getJavaFxVersion());
        assertEquals("javafx-base-" + expectedVersion + "-linux.jar",
                BootLoader.getJavaFxFileName("base", "linux"));
        assertEquals("https://repo1.maven.org/maven2/org/openjfx/javafx-base/" + expectedVersion
                        + "/javafx-base-" + expectedVersion + "-linux.jar",
                BootLoader.getJavaFxDownloadUrl("base", "linux"));
    }
}
