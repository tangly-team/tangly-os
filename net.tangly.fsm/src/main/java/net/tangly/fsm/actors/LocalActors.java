/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.fsm.actors;

import net.tangly.fsm.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The actor infrastructure enabler is responsible to provide parallel execution for all instantiated and alive actors he is in charge.
 */
public class LocalActors<E extends Enum<E>> implements Actors<E> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalActors.class);
    private static final LocalActors<?> instance = new LocalActors<>();
    private final Map<String, Actor<E>> actors;
    private final ExecutorService executor;

    /**
     * Default constructor.
     */
    private LocalActors() {
        actors = new ConcurrentHashMap<>();
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Returns the local actors instance.
     *
     * @return the local actors instance
     */
    public static LocalActors instance() {
        return instance;
    }

    @Override
    public void sendEventTo(@NotNull Event<E> event, @NotNull String name) {
        Actor<E> actor = getActorNamed(name);
        if (actor != null) {
            actor.receive(event);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Actor<E>> T getActorNamed(@NotNull String name) {
        return (T) actors.get(name);

    }

    @Override
    public <T extends Actor<E>> void register(@NotNull T actor) {
        actors.put(actor.name(), actor);
        executor.submit((LocalActor) actor);
    }

    @Override
    public <T extends Actor<E>> void awaitCompletion(@NotNull T actor, int intervalInMilliseconds) {
        awaitCompletion(Collections.singleton(actor), intervalInMilliseconds);
    }

    @Override
    public <T extends Actor<E>> void awaitCompletion(@NotNull Set<T> actors, int intervalInMilliseconds) {
        while (actors.stream().anyMatch(Actor::isAlive)) {
            try {
                Thread.sleep(intervalInMilliseconds);
            } catch (InterruptedException e) {
                log.error("LocalActors encountered interrupted exception", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
