/*
 * Copyright 2024 Marcel Baumann
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

import java.util.stream.Stream;

/**
 * An alternative approach to recursion is using an infinite Stream pipeline with the recursive call wrapped in a functional interface.
 *
 * @param <T>
 */
@FunctionalInterface
public interface RecursiveCall<T> {
    RecursiveCall<T> apply();

    default boolean isComplete() {
        return false;
    }

    default T result() {
        return null;
    }

    default T run() {
        return Stream.iterate(this, RecursiveCall::apply).filter(RecursiveCall::isComplete).findFirst().get().result();
    }

    static <T> RecursiveCall<T> done(T value) {
        return new RecursiveCall<T>() {
            @Override
            public boolean isComplete() {
                return true;
            }

            @Override
            public T result() {
                return value;
            }

            @Override
            public RecursiveCall<T> apply() {
                return this;
            }
        };
    }
}
