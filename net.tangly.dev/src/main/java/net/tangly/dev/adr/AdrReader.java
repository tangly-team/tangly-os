/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.dev.adr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.nio.file.Files.readAllLines;

/**
 * Imports architecture design records <em>ADR</em> from asciidoc files.
 */
public class AdrReader {
    private static final Logger logger = LogManager.getLogger();

    public AdrReader() {
    }

    public static void main(String[] args) {
        String rootFolder = "/Users/Shared/Projects/tangly-os";

        Path[] paths = {
            Paths.get(rootFolder, "net.tangly.bdd/src/site/adr"), Paths.get(rootFolder, "net.tangly.bus/src/site/adr"), Paths.get(rootFolder,
            "net.tangly.commons/src/site/adr"), Paths.get(rootFolder, "net.tangly.core/src/site/adr"), Paths.get(rootFolder,
            "net.tangly.dev/src/site/adr"), Paths.get(rootFolder, "net.tangly.erp-integration/src/site/adr"), Paths.get(rootFolder,
            "net.tangly.fsm/src/site/adr"), Paths.get(rootFolder, "net.tangly.gleam/src/site/adr"), Paths.get(rootFolder,
            "net.tangly.ports/src/site/adr"), Paths.get(rootFolder, "net.tangly.ui/src/site/adr"), Paths.get(rootFolder, "src/site/website/content/ideas/adr")
        };
        AdrReader reader = new AdrReader();
        List<Adr> records = reader.readAll(paths);
    }


    public Adr read(@NotNull Path path) {
        try {
            var lines = readAllLines(path);
            var index = 0;
            index = skipWhile(lines, index, o -> !o.startsWith("=="));
            Adr adr = new Adr(extractId(lines.get(index)));
            adr.title(extractTitle(lines.get(index)));
            adr.module(adr.id().substring(0, adr.id().indexOf("-")).toLowerCase());
            adr.adrPath(path);
            index++;

            index = skipWhile(lines, index, String::isBlank);
            adr.date(extractDate(lines.get(index)));
            index++;
            index = skipWhile(lines, index, String::isBlank);

            index = skipWhile(lines, index, o -> !o.startsWith("=== Status"));
            index++;
            index = skipWhile(lines, index, String::isBlank);
            adr.status(lines.get(index).strip());

            // [optional] === Links (one link per line)
            index = skipWhile(lines, index, o -> !o.startsWith("==="));
            if (lines.get(index).startsWith("=== Links")) {

            }

            int contextStart = skipWhile(lines, index, o -> !o.startsWith("=== Context"));
            int decisionStart = skipWhile(lines, index, o -> !o.startsWith("=== Decision"));
            adr.context(extractBlock(lines, contextStart + 1, decisionStart - 1));
            int consequenceStart = skipWhile(lines, index, o -> !o.startsWith("=== Consequence"));
            adr.decision(extractBlock(lines, decisionStart + 1, consequenceStart - 1));
            adr.consequences(extractBlock(lines, consequenceStart + 1, lines.size() - 1));
            logger.atInfo().log("ADR from {} imported", path);
            return adr;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Adr> readAll(Path... folderPaths) {
        List<Adr> records = new ArrayList<>();
        for (Path folderPath : folderPaths) {
            if (Files.isDirectory(folderPath)) {
                logger.atInfo().log("Importing ADR from directory {}", folderPath);
                try (var paths = Files.newDirectoryStream(folderPath)) {
                    for (Path path : paths) {
                        if (path.toString().endsWith(".adoc")) {
                            records.add(read(path));
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return records;
    }

    private String extractId(@NotNull String line) {
        String id = line.substring(line.indexOf("==") + 2).strip();
        return id.substring(0, id.indexOf(" "));
    }

    private String extractTitle(@NotNull String line) {
        var title = line.substring(line.indexOf("==") + 2).strip();
        return title.substring(title.indexOf(" ")).trim();
    }

    private LocalDate extractDate(@NotNull String line) {
        var date = line.substring(line.indexOf("Date:") + 5).strip();
        return LocalDate.parse(date);
    }

    private String extractBlock(@NotNull List<String> lines, int start, int end) {
        int index = skipWhile(lines, start, String::isBlank);
        var buffer = new StringBuilder();
        for (int i = index; i <= end; i++) {
            buffer.append(lines.get(i)).append(System.lineSeparator());
        }
        return buffer.toString();
    }

    private int skipWhile(@NotNull List<String> lines, int start, @NotNull Predicate<String> predicate) {
        int index = start;
        while (predicate.test(lines.get(index))) {
            index++;
        }
        return index;
    }
}
