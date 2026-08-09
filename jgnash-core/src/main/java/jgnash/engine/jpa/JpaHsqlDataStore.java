/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2020 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.engine.jpa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import jgnash.engine.DataStoreType;
import jgnash.engine.Engine;
import jgnash.util.FileUtils;
import jgnash.util.NotNull;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.DatabaseType;

/**
 * JPA specific code for HSQLDB data storage and creating an engine.
 *
 * @author Craig Cavanaugh
 */
public class JpaHsqlDataStore extends AbstractJpaDataStore {

    public static final String FILE_EXT = ".script";

    public static final String LOCK_EXT = ".lck";

    private static final long LOCK_RELEASE_TIMEOUT = 30L * 1000L;

    private static final String[] extensions = new String[]{".log", ".properties", FILE_EXT, ".data", ".backup",
            ".tmp", ".lobs", LOCK_EXT};

    @Override
    public Engine getLocalEngine(final String fileName, final String engineName, final char[] password) {
        final Engine engine = super.getLocalEngine(fileName, engineName, password);

        if (engine == null) {
            // Failed authentication can leave HSQLDB background resources and file handles open.
            final String databasePath = FileUtils.stripFileExtension(fileName);
            final Database database = DatabaseManager.lookupDatabaseObject(DatabaseType.DB_FILE, databasePath);
            if (database != null) {
                database.close(Database.CLOSEMODE_IMMEDIATELY);
            }
            if (!FileUtils.waitForFileRemoval(databasePath + LOCK_EXT, LOCK_RELEASE_TIMEOUT)) {
                logger.warning("Timed out waiting for the failed HSQLDB connection to release its lock");
            }
        }

        return engine;
    }

    @NotNull
    @Override
    public String getFileExt() {
        return FILE_EXT;
    }

    @Override
    public DataStoreType getType() {
        return DataStoreType.HSQL_DATABASE;
    }

    @Override
    public void rename(final String fileName, final String newFileName) throws IOException {

        for (final String extension : extensions) {
            final Path path = Paths.get(FileUtils.stripFileExtension(fileName) + extension);

            if (Files.exists(path)) {
                Files.move(path, Paths.get(FileUtils.stripFileExtension(newFileName) + extension));
            }
        }
    }

    @Override
    public String getLockFileExtension() {
        return LOCK_EXT;
    }

    @Override
    public void deleteDatabase(final String fileName) {

        final String base = FileUtils.stripFileExtension(fileName);

        for (final String extension : extensions) {
            try {
                Files.deleteIfExists(Paths.get(base + extension));
            } catch (final IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
