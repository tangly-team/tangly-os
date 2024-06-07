/*
 * Copyright 2023-2024 Marcel Baumann
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

package net.tangly.ui.components;

import com.vaadin.flow.data.binder.Binder;
import org.jetbrains.annotations.NotNull;

/**
 * Defines an abstraction which binds property values of an entity to a binder object. The mode of a visual domain is supported.
 *
 * @param <T> type of the instance which properties are displayed
 */
public interface HasBindValue<T> {
    T value();

    void value(T item);

    Mode mode();

    void mode(@NotNull Mode mode);

    default void bind(@NotNull Binder<T> binder, boolean readonly) {
    }
}
