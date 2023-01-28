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

import net.tangly.fsm.eventbus.EventBus;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EventBusAsynchron implements EventBus {
    private final String name;
    private final Map<Class<?>, Set<Consumer<?>>> consumers;
    private final ExecutorService executor;

    record Call<E>(Consumer<E> consumer, E event) implements Runnable {
        @Override
        public void run() {
            consumer.accept(event);
        }
    }

    public EventBusAsynchron(String name) {
        this.name = name;
        consumers = new ConcurrentHashMap<>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <E> void register(Consumer<E> consumer, Class<? extends E> eventType) {
        consumers.computeIfAbsent(eventType, k -> new ConcurrentSkipListSet<Consumer<?>>()).add(consumer);
    }

    @Override
    public <E> void unregister(Consumer<E> consumer, Class<? extends E> eventType) {
        if (consumers.containsKey(eventType)) {
            consumers.get(eventType).remove(consumer);
        }
    }

    @Override
    public <E> boolean isRegistered(Consumer<E> consumer, Class<? extends E> eventType) {
        return false;
    }

    @Override
    public <E> void publish(E event) {
        Class<?> clazz = event.getClass();
        while (clazz != null) {
            if (consumers.containsKey(clazz)) {
                consumers.get(clazz).forEach(o -> executor.submit(new Call(o, event)));
            }
            clazz = clazz.getSuperclass();
        }
    }
}
