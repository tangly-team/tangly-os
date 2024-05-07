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

package net.tangly.commons.lang;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A reference to an object, emulating the concept of typed pointer to typed variable. The indirection can be updated to emulate indirection such as pointer to
 * pointer in other languages.
 *
 * @param <T> type of the reference
 */
public class Reference<T> {
    private T reference;

    /**
     * Constructor of the class.
     *
     * @param reference reference object
     */
    public Reference(T reference) {
        this.reference = reference;
    }

    /**
     * Constructor of the class to build an empty reference.
     */
    public Reference() {
    }

    /**
     * Return a new reference to the given object.
     *
     * @param reference referenced object
     * @param <T>       type of the reference
     * @return new reference
     */
    public static <T> Reference<T> of(T reference) {
        return new Reference<>(reference);
    }

    /**
     * Return a new empty reference to the given object.
     *
     * @param <T> type of the reference
     * @return new empty reference
     */
    public static <T> Reference<T> empty() {
        return new Reference<>();
    }

    /**
     * Set the referenced object.
     *
     * @param reference referenced object
     * @see #get()
     */
    public void set(T reference) {
        this.reference = reference;
    }

    /**
     * Return the reference object.
     *
     * @return referenced object
     * @see #set(Object)
     */

    public T get() {
        return reference;
    }

    /**
     * If a value is not present, returns true, otherwise false.
     *
     * @return true if a value is not present, otherwise false
     */
    public boolean isEmpty() {
        return reference == null;
    }

    /**
     * If a value is present, returns true, otherwise false.
     *
     * @return true if a value is present, otherwise false
     */
    public boolean isPresent() {
        return reference != null;
    }

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action the action to be performed, if a value is present
     */
    public void ifPresent(@NotNull Consumer<? super T> action) {
        if (reference != null) {
            action.accept(reference);
        }
    }
}
