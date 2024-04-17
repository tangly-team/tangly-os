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

package net.tangly.core;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Define a mixin with an absolute time range. The time range can be open on one or both sides.
 */
public interface HasDateRange {
    /**
     * String representation of the property associated with the mixin.
     */
    String FROM = "from";

    /**
     * String representation of the property associated with the mixin.
     */
    String TO = "to";

    /**
     * Return the range of the mixin.
     *
     * @return date range of the mixin
     * @see #range(DateRange)
     */
    DateRange range();

    /**
     * Set the starting date when the entity is existing and active.
     *
     * @param range the new period for the entity
     * @see #range()
     */
    default void range(DateRange range) {
        throw new IllegalCallerException("Trait is in immutable form");
    }

    /**
     * Return the date from when the entity is existing and active.
     *
     * @return the start of the existing period for the entity
     * @see #from(LocalDate)
     */
    default LocalDate from() {
        return range().from();
    }

    /**
     * Set a new start to the date range.
     *
     * @param from new start date.
     * @see #from()
     */
    default void from(LocalDate from) {
        range(range().from(from));
    }

    /**
     * Return the date until when the entity is existing and active.
     *
     * @return the end of the existing period for the entity
     * @see #to(LocalDate)
     */
    default LocalDate to() {
        return range().to();
    }

    /**
     * Set a new end to the date range.
     *
     * @param to new start date.
     * @see #to()
     */
    default void to(LocalDate to) {
        range(range().to(to));
    }

    /**
     * Test if the date range is partially inside the date range specified in the filter.
     *
     * @param range date range to test against
     * @param <T>   type of instances to test
     */
    record RangeFilter<T extends HasDateRange>(@NotNull DateRange range) implements Predicate<T> {
        public RangeFilter(LocalDate from, LocalDate to) {
            this(DateRange.of(from, to));
        }

        public boolean test(@NotNull T entity) {
            return (Objects.isNull(range.to()) || Objects.isNull(entity.from()) || !entity.from().isBefore(range.to())) &&
                (Objects.isNull(range.from()) || Objects.isNull(entity.to()) || !entity.to().isBefore(range.from()));
        }
    }

    /**
     * Return true if the date is in the time range of the instance.
     *
     * @param date date against which the inclusion test is evaluated
     * @return true if inside the range otherwise false
     */
    default boolean isActive(@NotNull LocalDate date) {
        return range().isActive(date);
    }

    /**
     * Return true if the date now is in the time range of the instance.
     *
     * @return true if inside the range otherwise false
     */
    default boolean isActive() {
        return range().isActive();
    }
}
