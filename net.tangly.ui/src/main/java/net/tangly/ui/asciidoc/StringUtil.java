/*
 * Copyright 2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.asciidoc;

import net.tangly.core.Strings;

public final class StringUtil {
    public static String nullSafeString(String value) {
        return nullSafeString(value, "");
    }

    public static String nullSafeString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }


    public static String emptySafeString(String value, String defaultValue) {
        return Strings.isNullOrBlank(value) ? defaultValue : value;
    }

    public static String emptySafeString(String value) {
        return emptySafeString(value, "");
    }

    public static String toValid3ByteUTF8String(String s) {
        final int length = s.length();
        StringBuilder b = new StringBuilder(length);
        for (int offset = 0; offset < length; ) {
            final int codepoint = s.codePointAt(offset);
            if (codepoint > "\uFFFF".codePointAt(0)) {
                b.append("\uFFFD");
            } else {
                if (Character.isValidCodePoint(codepoint)) {
                    b.appendCodePoint(codepoint);
                } else {
                    b.append("\uFFFD");
                }
            }
            offset += Character.charCount(codepoint);
        }
        return b.toString();
    }
}
