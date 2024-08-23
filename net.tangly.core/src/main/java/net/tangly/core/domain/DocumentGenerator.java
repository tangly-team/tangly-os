/*
 * Copyright 2024 Marcel Baumann
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

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The generator is responsible for creating a document from an entity and a set of properties. The properties are used to configure the generation process.
 * The configuration can also be defined in the constructor of the generator.
 * <p>The generator instance can be discarded after the generation of the document.</p>
 *
 * @param <T> type of the entity used to create the document
 */
public interface DocumentGenerator<T> {
    Pattern PATTERN = Pattern.compile("\\d{4}-.*");
    String ORGANIZATION = "organization";
    String COPYRIGHT = "copyright";
    String LOGO = "logo";
    String THEME = "theme";
    String DOCUMENTS_PATH = "documentsFolder";
    String THEME_PATH = "themeFolder";

    /**
     * Creates a new output document.
     *
     * @param entity     entity used to create a new invoice document
     * @param properties properties to configure the creation process of the document
     * @param audit      domain audit sink to log the operation events
     */
    void export(@NotNull T entity, @NotNull Map<String, Object> properties, boolean overwrite, @NotNull DomainAudit audit);

    default boolean shouldExport(@NonNull Path document, boolean overwrite) {
        return overwrite || !Files.exists(document);
    }

    /**
     * Resolves the uri to where a document should be located in the file system. The convention is <em>base directory/year</em>. If folders do not
     * exist, they are created. The year must contain four digits.
     *
     * @param directory base directory containing all reports and documents
     * @param filename  filename of the document to write. The filename can contain a year as a prefix <code>YYYY-</code>.
     * @return uri to the folder where the document should be written. If the file does not contain a year, the base directory is returned
     */
    static Path resolvePath(@NotNull Path directory, @NotNull String filename) {
        var matcher = PATTERN.matcher(filename);
        var filePath = matcher.matches() ? directory.resolve(filename.substring(0, 4)) : directory;
        createDirectories(filePath);
        return filePath;
    }

    /**
     * Resolves the uri to where a document should be located in the file system. The convention is <em>base directory/year</em>. Non-existing folders are
     * created.
     *
     * @param directory base directory containing all reports and documents
     * @param year      year of the document
     * @param filename  filename of the document to write
     * @return uri to the document
     */
    static Path resolvePath(@NotNull Path directory, int year, @NotNull String filename) {
        var filePath = directory.resolve(Integer.toString(year));
        createDirectories(filePath);
        return filePath.resolve(filename);
    }

    /**
     * Recursively creates the directories if they do not exist.
     *
     * @param directory directory to create
     */
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
