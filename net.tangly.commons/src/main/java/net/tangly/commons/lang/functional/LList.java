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

package net.tangly.commons.lang.functional;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;

/**
 * Defines an immutable list structure as used in Lisp or Scheme. The fundamental data structure in many functional languages is the immutable linked list. It
 * is constructed from two building blocks:
 * <dl>
 * <dt>Nil</dt><dd>The empty list</dd>
 * <dt>Cons</dt><dd>A cell containing an element also known as head; and a pointer to the rest of the list also known as tail.</dd>
 * </dl>
 * <p>The term <em>cons</em> comes from functional programming language <em>Lisp</em> or <em>Scheme</em>. These kind of data structures are called persistent
 * data structures. Since the data structure is immutable, one might think that a lot of memory would be consumed since a new list would be created when we try
 * to perform various operations on list. On the contrary, these kind of data structures allow structural sharing by sharing nodes. A persistent data structure
 * does preserve the previous version of itself when being modified and is therefore <em>effectively</em> immutable. Fully persistent data structures allow both
 * updates and queries on any version.</p>
 *
 * @param <T> type of the list items
 */
public sealed interface LList<T> permits LList.Nil, LList.ImmutableList {
    /**
     * Defines a split iterator for the immutable persistent list structure. Split iterator is used to provide a {@link Stream} for an instance of the list.
     *
     * @param <T> type of the list items
     */
    class LListSplitIterator<T> implements Spliterator<T> {
        private final LList<T> list;
        private LList<T> pointer;

        public LListSplitIterator(@NotNull LList<T> list) {
            this.list = list;
            this.pointer = list;
        }

        @Override
        public boolean tryAdvance(@NotNull Consumer<? super T> action) {
            if (!pointer.isEmpty()) {
                action.accept(pointer.head());
                pointer = pointer.tail();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return list.length();
        }

        @Override
        public int characteristics() {
            return Spliterator.IMMUTABLE | Spliterator.SIZED;
        }
    }

    static <T> LList<T> nil() {
        return Nil.nil();
    }

    static <T> LList<T> cons(@NotNull T head, @NotNull LList<T> tail) {
        return new ImmutableList<>(head, tail);
    }

    @SafeVarargs
    static <T> LList<T> list(@NotNull T... items) {
        LList<T> list = LList.nil();
        for (int i = items.length - 1; i >= 0; i--) {
            list = cons(items[i], list);
        }
        return list;
    }

    boolean isEmpty();

    /**
     * Returns the head of the list. Is equivalent to the <em>car</em> Scheme operation.
     *
     * @return head item of the list
     */
    T head();

    /**
     * Returns the tail of the list. Is equivalent to the <em>cdr</em> Scheme operation.
     *
     * @return tail of the list
     */
    LList<T> tail();

    default int length() {
        return isEmpty() ? 0 : 1 + tail().length();
    }

    default LList<T> copy() {
        return isEmpty() ? LList.nil() : cons(head(), tail().copy());
    }

    default LList<T> append(@NotNull LList<T> list) {
        return cons(this.head(), tail().isEmpty() ? list : tail().append(list));
    }

    default <R> LList<R> map(@NotNull Function<T, ? extends R> function) {
        return cons(function.apply(this.head()), tail().isEmpty() ? nil() : tail().map(function));
    }

    default Stream<T> stream() {
        return StreamSupport.stream(new LListSplitIterator<>(this), false);
    }

    final class Nil<T> implements LList<T> {
        private static final Nil<?> Nil = new Nil<>();

        private Nil() {
        }

        public static <T> Nil<T> nil() {
            return (Nil<T>)Nil;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public T head() {
            throw new NoSuchElementException("head of empty list");
        }

        @Override
        public LList<T> tail() {
            throw new UnsupportedOperationException("tail of empty list");
        }

        @Override
        public String toString() {
            return "";
        }
    }

    record ImmutableList<T>(@NotNull T head, @NotNull LList<T> tail) implements LList<T> {
        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public String toString() {
            return head() + (tail().isEmpty() ? "" : ", " + tail().toString());
        }
    }
}
