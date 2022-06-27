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

package net.tangly.commons.lang.functional;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Functional programming strives to minimize side effects, so throwing exceptions is avoided. Instead, if an operation can fail, it should return a
 * representation of its outcome including an indication of success or failure, as well as its result (if successful), or some error data otherwise. In other
 * words, errors in functional progamming are just payload.
 * <p>Try should be preferred in functional programming approaches in particular in the context of stream pipelines.</p>
 *
 * @param <T> type of the computed result in the successful case
 */
public sealed interface Try<T> permits Try.Success, Try.Failure {
    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    Try<T> onSuccess(@NotNull Consumer<T> consumer);

    Try<T> onFailure(@NotNull Consumer<Throwable> consumer);

    Throwable getThrown();

    T get();

    <U> Try<U> map(@NotNull Function<? super T, ? extends U> fn);

    <U> Try<U> flatMap(@NotNull Function<? super T, Try<U>> fn);

    static <T> Try<T> failure(@NotNull Throwable t) {
        Objects.requireNonNull(t);
        return new Failure<>(t);
    }

    static <V> Success<V> success(@NotNull V value) {
        Objects.requireNonNull(value);
        return new Success<>(value);
    }

    static <T> Try<T> of(@NotNull Supplier<T> fn) {
        Objects.requireNonNull(fn);
        try {
            return Try.success(fn.get());
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    final class Failure<T> implements Try<T> {
        private final RuntimeException exception;

        public Failure(@NotNull Throwable t) {
            this.exception = new RuntimeException(t);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T get() {
            throw this.exception;
        }

        @Override
        public Try<T> onSuccess(@NotNull Consumer<T> consumer) {
            return this;
        }

        @Override
        public Try<T> onFailure(@NotNull Consumer<Throwable> consumer) {
            consumer.accept(getThrown());
            return this;
        }

        @Override
        public <U> Try<U> map(@NotNull Function<? super T, ? extends U> fn) {
            Objects.requireNonNull(fn);
            return Try.failure(this.exception);
        }

        @Override
        public <U> Try<U> flatMap(@NotNull Function<? super T, Try<U>> fn) {
            Objects.requireNonNull(fn);
            return Try.failure(this.exception);
        }

        @Override
        public Throwable getThrown() {
            return this.exception;
        }
    }

    final class Success<T> implements Try<T> {
        private final T value;

        public Success(@NotNull T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public Try<T> onSuccess(@NotNull Consumer<T> consumer) {
            consumer.accept(get());
            return this;
        }

        @Override
        public Try<T> onFailure(@NotNull Consumer<Throwable> consumer) {
            return this;
        }

        @Override
        public <U> Try<U> map(@NotNull Function<? super T, ? extends U> fn) {
            Objects.requireNonNull(fn);
            try {
                return Try.success(fn.apply(this.value));
            } catch (Exception e) {
                return Try.failure(e);
            }
        }

        @Override
        public <U> Try<U> flatMap(@NotNull Function<? super T, Try<U>> fn) {
            Objects.requireNonNull(fn);
            try {
                return fn.apply(this.value);
            } catch (Exception e) {
                return Try.failure(e);
            }
        }

        @Override
        public Throwable getThrown() {
            throw new IllegalStateException("Success never has an exception");
        }
    }
}
