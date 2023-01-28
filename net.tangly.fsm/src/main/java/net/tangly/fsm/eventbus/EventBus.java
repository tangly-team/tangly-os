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

import net.tangly.fsm.eventbus.imp.EventBusAsynchron;
import net.tangly.fsm.eventbus.imp.EventBusSynchron;

import java.util.function.Consumer;

public interface EventBus {
    record EventConsumer<E>(Object target, Class<E> eventType, Consumer<E> consume) {
        static <E> EventConsumer<E> of(Object target, Class<E> eventType, Consumer<E> consume) {
            return new EventConsumer<>(target, eventType, consume);
        }
    }

    static EventBus ofSynchron(String name) {
        return new EventBusSynchron(name);
    }

    static EventBus ofAsynchron(String name) {
        return new EventBusAsynchron(name);
    }

    String name();

    <E> void register(Consumer<E> consumer, Class<? extends E> eventType);

    <E> void unregister(Consumer<E> consumer, Class<? extends E> eventType);

    <E> boolean isRegistered(Consumer<E> consumer, Class<? extends E> eventType);

    <E> void publish(E event);
}
