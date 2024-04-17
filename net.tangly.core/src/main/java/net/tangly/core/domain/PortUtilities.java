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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class PortUtilities {
    private static final Pattern PATTERN = Pattern.compile("\\d{4}-.*");

    private PortUtilities() {
    }

    /**
     * Resolve the path to where a document should be located in the file system. The convention is <em>base directory/year</em>. If folders do not
     * exist, they are created. The year must contain four digits.
     *
     * @param directory base directory containing all reports and documents
     * @param filename  filename of the document to write
     * @return path to the folder where the document should be written. If the file does not contain a year, the base directory is returned
     */
    public static Path resolvePath(@NotNull Path directory, @NotNull String filename) {
        var matcher = PATTERN.matcher(filename);
        var filePath = matcher.matches() ? directory.resolve(filename.substring(0, 4)) : directory;
        if (Files.notExists(filePath)) {
            try {
                Files.createDirectories(filePath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return filePath;
    }
}
