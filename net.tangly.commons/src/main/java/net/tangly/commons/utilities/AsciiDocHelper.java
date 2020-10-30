/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.utilities;

import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;

import net.tangly.commons.lang.Strings;
import org.jetbrains.annotations.NotNull;

public class AsciiDocHelper {
    public static final String NEWLINE = " +" + System.lineSeparator();
    private static final DecimalFormat DF = new DecimalFormat("#,##0.00");

    private final PrintWriter writer;

    public AsciiDocHelper(@NotNull Writer writer) {
        this.writer = new PrintWriter(writer);
    }

    public PrintWriter writer() {
        return writer;
    }

    public static String bold(String text) {
        return "*" + text + "*";
    }

    public static String italics(String text) {
        return text.length() > 0 ? ("_" + text + "_") : "";
    }

    /**
     * Formats a decimal value into an accounting representation with two digits after the separator, colored in read if negative, and discarded if the value is
     * zero.
     *
     * @param value decimal value to format
     * @return the formatted string
     */
    public static String format(@NotNull BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            return DF.format(value);
        } else if (value.compareTo(BigDecimal.ZERO) < 0) {
            return "[red]#" + DF.format(value) + "#";
        } else {
            return "";
        }
    }

    public AsciiDocHelper header(String text, int level) {
        writer.append("=".repeat(level)).append(" ").append(text).println();
        writer.println();
        return this;
    }

    public AsciiDocHelper text(String text) {
        writer.append(text);
        return this;
    }

    public AsciiDocHelper paragraph(String text) {
        writer.append(text).println();
        writer.println();
        return this;
    }

    public AsciiDocHelper tableHeader(String tableName, String attributes, String... headerCells) {
        if (Strings.emptyToNull(tableName) != null) {
            writer.append(".").println(tableName);
        }
        if (attributes != null) {
            writer.println("[" + attributes + "]");
        }
        writer.println("|===");
        if (headerCells.length != 0) {
            Arrays.asList(headerCells).forEach(o -> writer.append("^|").append(o).append(" "));
            writer.println();
        }
        return this;
    }

    public AsciiDocHelper tableRow(String... cells) {
        Arrays.asList(cells).forEach(o -> writer.append("|").append(o));
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

    public AsciiDocHelper image(String path, String config) {
        writer.append("image::").append(path).append("[").append(Strings.nullToEmpty(config)).println("]");
        return this;
    }
}
