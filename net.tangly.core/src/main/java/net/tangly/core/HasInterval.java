/*
 * Copyright 2006-2022 Marcel Baumann
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

import java.time.LocalDate;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

/**
 * Defines a mixin with a time interval.
 */
public interface HasInterval {
    record DateFilter(LocalDate from, LocalDate to) implements Predicate<LocalDate> {
        public boolean test(@NotNull LocalDate date) {
            return (from == null || !from.isAfter(date)) && (to == null || !to.isBefore(date));
        }
    }

    record IntervalFilter<T extends HasInterval>(LocalDate from, LocalDate to) implements Predicate<T> {
        public boolean test(@NotNull T entity) {
            return (from == null || !from.isAfter(entity.fromDate())) && (to == null || !to.isBefore(entity.toDate()));
        }
    }

    /**
     * Returns the date from when the entity is existing and active.
     *
     * @return the start of the existing period of the entity
     * @see #fromDate(LocalDate)
     */
    LocalDate fromDate();

    /**
     * Sets the from date when the entity is existing and active.
     * @param fromDate the new start of the existing period of the entity
     * @see #fromDate()
     */
    default void fromDate(LocalDate fromDate) {
        throw new IllegalCallerException("Trait is in immutable form");
    }

    /**
     * Returns the date until when the entity is existing and active.
     *
     * @return the end of the existing period of the entity
     * @see #toDate(LocalDate)
     */
    LocalDate toDate();

    /**
     * Sets the end date when the entity is existing and active.
     * @param toDate the new end of the existing period of the entity
     * @see #toDate() ()
     */
    default void toDate(LocalDate toDate) {
        throw new IllegalCallerException("Trait is in immutable form");
    }


    /**
     * Returns true if the date now is in the time interval of the instance.
     *
     * @return true if inside the interval otherwise false
     */
    default boolean isActive() {
        return isActive(LocalDate.now());
    }

    /**
     * Returns true if the date now is in the time interval of the instance.
     *
     * @param date date against which the inclusion test is evaluated
     * @return true if inside the interval otherwise false
     */
    default boolean isActive(@NotNull LocalDate date) {
        return ((fromDate() == null) || (!date.isBefore(fromDate()))) && ((toDate() == null) || (!date.isAfter(toDate())));
    }

    static boolean isActive(@NotNull LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return ((fromDate == null) || (!date.isBefore(fromDate))) && ((toDate == null) || (!date.isAfter(toDate)));
    }
}
