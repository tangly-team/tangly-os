/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.utilities;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

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
}
