/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.lang;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the try abstraction in Java. Try can be used to return a function result in the context of streams. Streams do not accept methods throwing
 * exceptions.
 * <p>A try instance has either a result or a failure. This invariant is enforced in the constructor.</p>
 *
 * @param <T> type of the result
 */
public record Try<T>(T result, RuntimeException failure) {
    public Try {
        if (Objects.isNull(result) ^ Objects.isNull(failure)) {
            throw new IllegalArgumentException("Either result or failure are not null");
        }
    }

    public Try(@NotNull T result) {
        this(result, null);
    }

    public Try(@NotNull RuntimeException failure) {
        this(null, failure);
    }

    public static <T, R> Try<R> of(@NotNull Function<T, R> function, T value) {
        try {
            return new Try(function.apply(value));
        } catch (Exception e) {
            return new Try(e);
        }
    }

    public static <R> Try<R> of(@NotNull Supplier<R> supplier) {
        try {
            return new Try(supplier.get());
        } catch (Exception e) {
            return new Try(e);
        }
    }

    public boolean isFailure() {
        return failure() != null;
    }

    public boolean isSuccess() {
        return !isFailure();
    }

    public Optional<T> optional() {
        return Optional.ofNullable(result);
    }
}
