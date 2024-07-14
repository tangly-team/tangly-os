/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core.domain;

import net.tangly.commons.logger.EventData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Define the inbound and outbound communication port to the bounded domain. It is a secondary port in the DDD terminology.
 * <p>The realm is available to the port due to the default import and export functionality specific to this framework.</p>
 *
 * @param <R> Realm of the bounded domain
 */
public interface Port<R extends Realm> {
    Pattern PATTERN = Pattern.compile("\\d{4}-.*");

    /**
     * Import all entities of the bounded domain from the file system.
     * All TSV, JSON, TOML, and YAML files are imported.
     * The domain is responsible for the order of the import and the handling of the entities.
     * A bounded domain should not depend on other domains to perform the operation.
     *
     * @param audit domain audit sink to log the operation events
     * @see #exportEntities(DomainAudit)
     */
    void importEntities(@NotNull DomainAudit audit);

    /**
     * Export all entities of the bounded domain to the file system as TSV, JSON, TOML, and YAML files.
     * A bounded domain should not depend on other domains to perform the operation.
     *
     * @param audit domain audit sink to log the operation events
     * @see #importEntities(DomainAudit)
     */
    void exportEntities(@NotNull DomainAudit audit);

    /**
     * Clear all entities of the bounded domain. Upon execution, the domain is empty. Use with caution as the operation is not reversible.
     * Typically, the operation is used before an import to ensure that the domain is empty.
     *
     * @param audit domain audit sink to log the operation events
     */
    void clearEntities(@NotNull DomainAudit audit);

    /**
     * Return the realm containing all the entities of the bounded domain.
     *
     * @return realm of the bounded domain
     */
    R realm();

    static void entitiesCleared(@NotNull DomainAudit audit, @NotNull String entities) {
        audit.log(EventData.CLEAR_EVENT, EventData.Status.SUCCESS, "entities %s were cleared".formatted(entities), Collections.emptyMap());
    }

    /**
     * Resolve the path to where a document should be located in the file system. The convention is <em>base directory/year</em>. If folders do not
     * exist, they are created. The year must contain four digits.
     *
     * @param directory base directory containing all reports and documents
     * @param filename  filename of the document to write
     * @return path to the folder where the document should be written. If the file does not contain a year, the base directory is returned
     */
    static Path resolvePath(@NotNull Path directory, @NotNull String filename) {
        var matcher = PATTERN.matcher(filename);
        var filePath = matcher.matches() ? directory.resolve(filename.substring(0, 4)) : directory;
        createDirectories(filePath);
        return filePath;
    }

    static Path resolvePath(@NotNull Path directory, int year, String filename) {
        var filePath = directory.resolve(Integer.toString(year));
        createDirectories(filePath);
        return filePath.resolve(filename);
    }

    static void createDirectories(@NotNull Path directory) {
        if (Files.notExists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
