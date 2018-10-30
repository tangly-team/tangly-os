/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.utilities;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.Arrays;

public class AsciiDocHelper {
    private final PrintWriter writer;

    public AsciiDocHelper(@NotNull PrintWriter writer) {
        this.writer = writer;
    }

    public AsciiDocHelper header(String text, int level) {
        writer.append("=".repeat(level)).append(" ").append(text).println();
        writer.println();
        return this;
    }

    public AsciiDocHelper tableHeader(String tableName, String cols, String... headerCells) {
        writer.append(".").println(tableName);
        if (cols != null) {
            writer.println("[cols=\"" + cols + "\"]");
        }
        writer.println("|===");
        Arrays.asList(headerCells).forEach(o -> writer.append("^|").append(o).append(" "));
        writer.println();
        writer.println();
        return this;
    }

    public AsciiDocHelper tableRow(String... cells) {
        Arrays.asList(cells).forEach(o -> writer.append("|").println(o));
        writer.println();
        return this;
    }

    public AsciiDocHelper tableEnd() {
        writer.println("|===");
        writer.println();
        return this;
    }

    public AsciiDocHelper newPage() {
        writer.println("<<<");
        return this;
    }

    public AsciiDocHelper inlineAnchor(String anchor) {
        writer.append("[[").append(anchor).append("]]");
        return this;
    }

    public AsciiDocHelper internalXRef(String xref, String text) {
        writer.append("<<").append(xref).append(",").append(text).append(">>");
        return this;
    }
}
