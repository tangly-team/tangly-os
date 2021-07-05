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

package net.tangly.fsm.eventbus;

import org.jetbrains.annotations.NotNull;

public interface Consumer<E> {
    void consume(@NotNull E event);

    default void register(@NotNull EventBus eventBus, @NotNull Class<E> clazz) {
        eventBus.register(this::consume, clazz);
    }
}
