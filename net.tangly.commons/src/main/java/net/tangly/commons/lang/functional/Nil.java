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

final class Nil<T> implements LList<T> {
    private static final Nil Nil = new Nil();

    private Nil() {
    }

    public static <T> Nil<T> nil() {
        return Nil;
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
