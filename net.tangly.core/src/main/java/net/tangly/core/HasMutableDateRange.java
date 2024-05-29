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

import java.time.LocalDate;

/**
 * Define a mixin with an absolute time range. The time range can be open on one or both sides.
 */
public interface HasMutableDateRange extends HasDateRange {
    void range(DateRange range);


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
     * Set a new end to the date range.
     *
     * @param to new start date.
     * @see #to()
     */
    default void to(LocalDate to) {
        range(range().to(to));
    }
}
