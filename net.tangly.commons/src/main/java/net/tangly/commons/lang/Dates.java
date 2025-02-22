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

package net.tangly.commons.lang;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public final class Dates {
    private Dates() {
    }

    public static boolean isWithinRange(@NotNull LocalDate date, LocalDate from, LocalDate to) {
        return ((from == null) || !date.isBefore(from)) && ((to == null) || !date.isAfter(to));
    }

    public static LocalDate of(String date) {
        return Strings.isNullOrBlank(date) ? null : LocalDate.parse(date);
    }
}
