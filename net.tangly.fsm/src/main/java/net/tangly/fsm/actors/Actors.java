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

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The actor infrastructure enabler is responsible to provide parallel execution for all instantiated and alive actors he is in charge.
 *
 * @param <T> type of the messages exchanged between actors
 */
public interface Actors<T> {
    static void awaitTermination(ExecutorService service, long timeout, TimeUnit unit) {
        try {
            service.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends an event to the actor with the given name.
     *
     * @param message message to send
     * @param id      identifier of the actor
     */
    void sendMsgTo(@NotNull T message, @NotNull UUID id);

    /**
     * Returns an actor with the given name.
     *
     * @param name name of the actor to be found
     * @return the requested actor if found
     */
    Optional<Actor<T>> actorNamed(@NotNull String name);

    /**
     * Registers the actor in the pool of known actors.
     *
     * @param actor actor to register
     */
    void register(@NotNull Actor<T> actor);
}
