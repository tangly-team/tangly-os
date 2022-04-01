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

package net.tangly.fsm.eventbus.imp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.tangly.fsm.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

public class EventBusSynchron implements EventBus {
    private final String name;
    private final Map<Class<?>, Set<Consumer<?>>> consumers;

    public EventBusSynchron(String name) {
        this.name = name;
        consumers = new HashMap<>();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <E> void register(@NotNull Consumer<E> consumer, @NotNull Class<? extends E> eventType) {
        consumers.computeIfAbsent(eventType, HashSet::new).add(consumer);
    }

    @Override
    public <E> void unregister(@NotNull Consumer<E> consumer, @NotNull Class<? extends E> eventType) {
        if (consumers.containsKey(eventType)) {
            consumers.get(eventType).remove(consumer);
        }
    }

    @Override
    public <E> boolean isRegistered(@NotNull Consumer<E> consumer, @NotNull Class<? extends E> eventType) {
        return consumers.containsKey(eventType) && consumers.get(eventType).contains(consumer);
    }

    @Override
    public <E> void publish(@NotNull E event) {
        Class<?> clazz = event.getClass();
        while (clazz != null) {
            if (consumers.containsKey(clazz)) {
                consumers.get(clazz).forEach(o -> ((Consumer<E>) o).accept(event));
            }
            clazz = clazz.getSuperclass();
        }
    }
}
