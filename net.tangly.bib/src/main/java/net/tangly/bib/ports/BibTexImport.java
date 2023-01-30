/*
 * Copyright 2022-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.bib.ports;

import net.tangly.bib.model.Book;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BibTexImport {
    public BibTexImport() {}

    public void parse(Path path) throws ParseException, IOException {
        try (Reader in = Files.newBufferedReader(path)) {
            BibTeXParser parser = new BibTeXParser();
            BibTeXDatabase database = parser.parseFully(in);
        }
    }

    private List<Book> imports(BibTeXDatabase database) {
        return database.getEntries().values().stream()
                       .filter(o -> BibTeXEntry.TYPE_BOOK.equals(o.getKey()))
                       .map(o -> new Book(o))
                       .toList();
    }
}
