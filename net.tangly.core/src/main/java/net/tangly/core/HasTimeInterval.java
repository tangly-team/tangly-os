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
 * Defines a mixin with an absolute time interval.
 */
public interface HasTimeInterval {
    /**
     * String representation of the property associated with the mixin.
     */
    String FROM = "from";

    /**
     * String representation of the property associated with the mixin.
     */
    String TO = "to";


    /**
     * Sets the from date when the entity is existing and active.
     *
     * @param fromDate the new start of the existing period of the entity
     * @see #from()
     */
    default void from(LocalDate fromDate) {
        throw new IllegalCallerException("Trait is in immutable form");
    }

    /**
     * Sets the end date when the entity is existing and active.
     *
     * @param toDate the new end of the existing period of the entity
     * @see #to() ()
     */
    default void to(LocalDate toDate) {
        throw new IllegalCallerException("Trait is in immutable form");
    }

    /**
     * Returns the date from when the entity is existing and active.
     *
     * @return the start of the existing period of the entity
     * @see #from(LocalDate)
     */
    LocalDate from();

    /**
     * Returns the date until when the entity is existing and active.
     *
     * @return the end of the existing period of the entity
     * @see #to(LocalDate)
     */
    LocalDate to();

    /**
     * Tests if the date is inside the time interval specified in the filter.
     *
     * @param from start of the time interval
     * @param to   end of the time interval
     */
    record DateFilter(LocalDate from, LocalDate to) implements Predicate<LocalDate> {
        public boolean test(@NotNull LocalDate date) {
            return (from == null || !from.isAfter(date)) && (to == null || !to.isBefore(date));
        }
    }


    /**
     * Tests if the time interval is partially inside the time interval specified in the filter.
     *
     * @param from start of the time interval
     * @param to   end of the time interval
     * @param <T>  type of instances to test
     */
    record IntervalFilter<T extends HasTimeInterval>(LocalDate from, LocalDate to) implements Predicate<T> {
        public boolean test(@NotNull T entity) {
            return (Objects.isNull(to()) || Objects.isNull(entity.from()) || !entity.from().isBefore(to())) &&
                (Objects.isNull(from()) || Objects.isNull(entity.to()) || !entity.to().isBefore(from()));
        }
    }

    /**
     * Returns true if the date is in the time interval of the instance.
     *
     * @param date date against which the inclusion test is evaluated
     * @return true if inside the interval otherwise false
     */
    default boolean isActive(@NotNull LocalDate date) {
        return ((from() == null) || (!date.isBefore(from()))) && ((to() == null) || (!date.isAfter(to())));
    }


    /**
     * Returns true if the date now is in the time interval of the instance.
     *
     * @return true if inside the interval otherwise false
     */
    default boolean isActive() {
        return isActive(LocalDate.now());
    }


    static boolean isActive(@NotNull LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return ((fromDate == null) || (!date.isBefore(fromDate))) && ((toDate == null) || (!date.isAfter(toDate)));
    }
}
