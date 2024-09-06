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

package net.tangly.commons.utilities;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class FileUtilities {
    private FileUtilities() {
    }

    public static void replaceInSelectedFileWithUnixEol(@NotNull Path directory, @NotNull Set<String> endings) {
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(file -> !Files.isDirectory(file) && endings.stream().anyMatch(o -> file.getFileName().toString().endsWith(o)))
                .forEach(FileUtilities::replaceWithUnixEol);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void replaceWithUnixEol(@NotNull Path path) {
        try {
            var text = Files.readString(path);
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
            Files.writeString(path, text);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Packs the content of the source folder into a single zip file.
     *
     * @param sourceDirPath the path to the source folder
     * @param zipFilePath   the path to the zip file
     * @throws UncheckedIOException if an IO error occurs
     */
    public static void pack(@NotNull Path sourceDirPath, @NotNull Path zipFilePath) {
        try {
            try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFilePath)); Stream<Path> paths = Files.walk(sourceDirPath)) {
                paths.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                    try {
                        zs.putNextEntry(zipEntry);
                        Files.copy(path, zs);
                        zs.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
