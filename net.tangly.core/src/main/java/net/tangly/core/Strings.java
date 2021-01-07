/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

import java.util.Objects;

/**
 * String utility class until the official API supports these operation.
 */
public final class Strings {
    private Strings() {
    }

    public static boolean isNullOrEmpty(String string) {
        return Objects.isNull(string) || string.isBlank();
    }

    public static boolean isNullOrBlank(String string) {
        return Objects.isNull(string) || string.isBlank();
    }

    public static String blankToNull(String string) {
        return isNullOrBlank(string) ? null : string;
    }

    public static String emptyToNull(String string) {
        return isNullOrEmpty(string) ? null : string;
    }

    public static String nullToEmpty(String string) {
        return Objects.isNull(string) ? "" : string;
    }

    public static String trim(String string) {
        return (string == null) ? null : string.trim();
    }

    public static String normalizeToNull(String string) {
        return isNullOrEmpty(string) ? null : string.trim();
    }
}
