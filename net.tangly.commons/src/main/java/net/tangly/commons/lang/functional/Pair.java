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

package net.tangly.commons.lang.functional;

/**
 * Defines a typed pair as long as JDK API is not providing the abstraction. Pairs can, for example, be used to return two values from a function. The regular
 * approach is Java is to define an additional record type.
 *
 * @param <T> left type of the pair
 * @param <U> right type of the pair
 */
public record Pair<T, U>(T left, U right) {
    public static <T, U> Pair<T, U> of(T left, U right) {
        return new Pair<>(left, right);
    }
}
