/*
 * Copyright 2006-2023 Marcel Baumann
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
 * Define a mixin with an absolute time interval. The time interval can be open on one or both sides.
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
     * Return the interval of the mixin.
     *
     * @return date interval of the mixin
     * @see #interval(DateRange)
     */
    DateRange interval();

    /**
     * Set the starting date when the entity is existing and active.
     *
     * @param interval the new period for the entity
     * @see #interval()
     */
    default void interval(DateRange interval) {
        throw new IllegalCallerException("Trait is in immutable form");
    }

    /**
     * Return the date from when the entity is existing and active.
     *
     * @return the start of the existing period for the entity
     * @see #from(LocalDate)
     */
    default LocalDate from() {
        return interval().from();
    }

    /**
     * Set a new start to the date range.
     *
     * @param from new start date.
     * @see #from()
     */
    default void from(LocalDate from) {
        interval(interval().from(from));
    }

    /**
     * Return the date until when the entity is existing and active.
     *
     * @return the end of the existing period for the entity
     * @see #to(LocalDate)
     */
    default LocalDate to() {
        return interval().to();
    }

    /**
     * Set a new end to the date range.
     *
     * @param to new start date.
     * @see #to()
     */
    default void to(LocalDate to) {
        interval(interval().to(to));
    }

    /**
     * Test if the date interval is partially inside the date interval specified in the filter.
     *
     * @param interval date interval to test against
     * @param <T>      type of instances to test
     */
    record IntervalFilter<T extends HasDateRange>(@NotNull DateRange interval) implements Predicate<T> {
        public IntervalFilter(LocalDate from, LocalDate to) {
            this(DateRange.of(from, to));
        }

        public boolean test(@NotNull T entity) {
            return (Objects.isNull(interval.to()) || Objects.isNull(entity.from()) || !entity.from().isBefore(interval.to())) &&
                (Objects.isNull(interval.from()) || Objects.isNull(entity.to()) || !entity.to().isBefore(interval.from()));
        }
    }

    /**
     * Return true if the date is in the time interval of the instance.
     *
     * @param date date against which the inclusion test is evaluated
     * @return true if inside the interval otherwise false
     */
    default boolean isActive(@NotNull LocalDate date) {
        return interval().isActive(date);
    }

    /**
     * Return true if the date now is in the time interval of the instance.
     *
     * @return true if inside the interval otherwise false
     */
    default boolean isActive() {
        return interval().isActive();
    }
}
