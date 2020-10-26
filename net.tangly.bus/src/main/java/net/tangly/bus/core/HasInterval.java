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

package net.tangly.bus.core;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Defines a mixin with a time interval.
 */
public interface HasInterval {
    class IntervalFilter<T extends HasInterval> implements Predicate<T> {
        private LocalDate from;
        private LocalDate to;

        public IntervalFilter(LocalDate from, LocalDate to) {
            interval(from, to);
        }

        public final void interval(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }

        public boolean test(T entity) {
            return (from == null || !from.isAfter(entity.fromDate())) && (to == null || !to.isBefore(entity.toDate()));
        }
    }

    /**
     * Return the date from when the entity is existing and active.
     *
     * @return the start of the existing period of the entity
     */
    LocalDate fromDate();

    /**
     * Return the date until when the entity is existing and active.
     *
     * @return the end of the existing period of the entity
     */
    LocalDate toDate();

    /**
     * Return true if the date now is in the time interval of the instance.
     *
     * @return true if inside the interval otherwise false
     */
    default boolean isActive() {
        return isActive(LocalDate.now());
    }

    /**
     * Return true if the date now is in the time interval of the instance.
     *
     * @param date date against which the inclusion test is evaluated
     * @return true if inside the interval otherwise false
     */
    default boolean isActive(LocalDate date) {
        return ((fromDate() == null) || (!date.isBefore(fromDate()))) && ((toDate() == null) || (!date.isAfter(toDate())));
    }
}
