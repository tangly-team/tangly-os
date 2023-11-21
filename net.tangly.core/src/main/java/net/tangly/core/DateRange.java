/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Define an absolute local date range. The range can be open, meaning either the lower or upper date can be null.
 */
public record DateRange(LocalDate from, LocalDate to) implements Serializable {
    public static final DateRange INFINITE = new DateRange();

    /**
     * Test if the date is inside the date interval specified in the filter.
     *
     * @param interval date interval to test against
     */
    public record DateFilter(@NotNull DateRange interval) implements Predicate<LocalDate> {
        public DateFilter(LocalDate from, LocalDate to) {
            this(DateRange.of(from, to));
        }

        public boolean test(@NotNull LocalDate date) {
            return (interval.from() == null || !interval.from().isAfter(date)) && (interval.to() == null || !interval.to().isBefore(date));
        }
    }

    /**
     * Creates a fully open date range.
     */
    public DateRange() {
        this(null, null);
    }

    /**
     * Factory method.
     *
     * @param from start of the date range. can be null
     * @param to   end of the date range. can be null
     * @return new date range
     */
    public static DateRange of(LocalDate from, LocalDate to) {
        return new DateRange(from, to);
    }

    /**
     * Create a new data range with the given lower bound.
     *
     * @param from new lower bound
     * @return new date range with the given lower bound
     * @see #to(LocalDate)
     */
    public DateRange from(LocalDate from) {
        return new DateRange(from, to());
    }

    /**
     * Create a new data range with the given upper bound.
     *
     * @param to new upper bound
     * @return new date range with the given upper bound
     * @see #from(LocalDate)
     */

    public DateRange to(LocalDate to) {
        return new DateRange(from(), to);
    }

    /**
     * Return true if the date is in the time interval of the instance.
     *
     * @param date date against which the inclusion test is evaluated
     * @return true if inside the interval otherwise false
     */
    public boolean isActive(@NotNull LocalDate date) {
        return ((from() == null) || (!date.isBefore(from()))) && ((to() == null) || (!date.isAfter(to())));
    }


    /**
     * Return true if the date now is in the time interval of the instance.
     *
     * @return true if inside the interval otherwise false
     */
    public boolean isActive() {
        return isActive(LocalDate.now());
    }

    /**
     * Return true if the date range is fully open.
     *
     * @return true if the date range is open in both directions.
     */
    public boolean isInfinite() {
        return Objects.isNull(from()) && Objects.isNull(to());
    }
}
