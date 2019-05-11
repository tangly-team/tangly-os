/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.utilities;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * Wrapper for a function throwing a checked exception to enable use in streams. ThrowingFunction.of(urlToCrawl -&gt; new URL(urlToCrawl))
 *
 * @param <T> parameter of the function
 * @param <R> return type of the function
 * @param <E> checked exception thrown by the function
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;

    static <T, R, E extends Exception> Function<T, R> of(ThrowingFunction<T, R, E> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        };
    }
}
