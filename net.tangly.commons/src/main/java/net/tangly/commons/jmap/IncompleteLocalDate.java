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

package net.tangly.commons.jmap;

import java.time.*;
import java.util.Objects;
import java.util.Optional;

/**
 * Models an incomplete local date such as a birthday. Incomplete dates are defined for example in the VCard standard.
 *
 * @param year  year of the date if defined otherwise 0
 * @param month month of the date if defined otherwise 0
 * @param day   day in the month of the deate if defined otherwise 0
 */
public record IncompleteLocalDate(int year, int month, int day) {
    public static IncompleteLocalDate of(int year, int month, int day) {
        return new IncompleteLocalDate(year, month, day);
    }

    public static IncompleteLocalDate of(int year, Month month, int day) {
        return new IncompleteLocalDate(year, (Objects.isNull(month) ? 0 : month.getValue()), day);
    }

    public boolean hasYear() {
        return year() != 0;
    }

    public boolean hasMonth() {
        return month() != 0;
    }

    public boolean hasDay() {
        return day() != 0;
    }

    public Optional<Year> toYear() {
        return Optional.empty();
    }

    public Optional<YearMonth> toYearMonth() {
        return Optional.empty();
    }

    public Optional<MonthDay> toMonthDay() {
        return Optional.empty();
    }

    public Optional<LocalDate> toDate() {
        return Optional.empty();
    }
}
