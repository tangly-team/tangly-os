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

package net.tangly.fsm.actors;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The actor infrastructure enabler is responsible to provide parallel execution for all instantiated and alive actors he is in charge.
 *
 * @param <T> the event enumeration type uniquely identifying the event sent to the state machine
 */
public class ActorsImp<T> implements Actors<T> {
    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<UUID, Actor<T>> actors;
    private final ExecutorService executor;

    /**
     * Default constructor.
     */
    public ActorsImp(ExecutorService executor) {
        actors = new ConcurrentHashMap<>();
        this.executor = executor;
    }

    @Override
    public void sendMsgTo(@NotNull T message, @NotNull UUID id) {
        if (actors.containsKey(id)) {
            actors.get(id).receive(message);
        }
    }

    @Override
    public Optional<Actor<T>> actorNamed(@NotNull String name) {
        return actors.values().stream().filter(o -> name.equals(o.name())).findAny();
    }

    @Override
    public void register(@NotNull Actor<T> actor) {
        actors.put(actor.id(), actor);
        executor.submit(actor);
    }
}
