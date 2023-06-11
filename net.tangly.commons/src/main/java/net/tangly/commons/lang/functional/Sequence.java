/*
 * Copyright 2022-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.lang.functional;

import org.jetbrains.annotations.Contract;

public interface Sequence<T> {
    /**
     * Returns the first element of the sequence.
     *
     * @return first element of the sequence if defined otherwise Nil
     */
    @Contract(pure = true)
    T first();

    /**
     * Returns the rest of the sequence witouth the first item of the sequence.
     *
     * @return rest of the sequence, can be empty
     */
    @Contract(pure = true)
    Sequence<T> rest();

    /**
     * Constructs a new immutable list by addind the new front item to the existing sequence.
     *
     * @param item     item to add to the sequence
     * @param sequence sequence where the item will be added in th front of the sequence
     * @return new immutable list
     */
    @Contract(pure = true)
    Sequence<T> cons(T item, Sequence<T> sequence);
}
