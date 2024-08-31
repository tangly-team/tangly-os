/*
 * Copyright 2022-2024 Marcel Baumann
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

package net.tangly.commons.lang.functional;

import org.jetbrains.annotations.Contract;

public interface Sequence<T> {
    /**
     * Return the first element of the sequence.
     *
     * @return first element of the sequence if defined otherwise Nil
     */
    @Contract(pure = true)
    T first();

    /**
     * Return the rest of the sequence without the first item of the sequence.
     *
     * @return rest of the sequence, it can be empty
     */
    @Contract(pure = true)
    Sequence<T> rest();

    /**
     * Construct a new immutable list by adding the new front item to the existing sequence.
     *
     * @param item     item to add to the sequence
     * @param sequence sequence where the item will be added in the front of the sequence
     * @return new immutable list
     */
    @Contract(pure = true)
    Sequence<T> cons(T item, Sequence<T> sequence);
}
