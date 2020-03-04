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

import java.util.Set;

/**
 * The actor infrastructure enabler is responsible to provide parallel execution for all instantiated and alive actors he is in charge.
 */
public interface Actors<E extends Enum<E>> {

    /**
     * Sends an event to the actor with the given name.
     *
     * @param event event to send
     * @param name  name of the actor whom the event will be sent
     */
    void sendEventTo(@NotNull Event<E> event, @NotNull String name);

    /**
     * Returns an actor with the given name.
     *
     * @param name name of the actor to be found
     * @param <T>  type of the actor extending actor class
     * @return the requested actor if found
     */
    <T extends Actor<E>> T getActorNamed(@NotNull String name);

    /**
     * Registers the actor in the pool of known actors.
     *
     * @param actor actor to register
     * @param <T>   type of the actor extending actor class
     */
    <T extends Actor<E>> void register(@NotNull T actor);

    /**
     * Awaits the completion of the given actor. An actor has completed his work when it is no more alive.
     *
     * @param actor                  actor to wait upon
     * @param intervalInMilliseconds interval in milliseconds between checking if the actor is alive
     * @param <T>                    type of the actor extending actor class
     */
    <T extends Actor<E>> void awaitCompletion(@NotNull T actor, int intervalInMilliseconds);

    /**
     * Awaits the completion of a set of actors. An actor has completed his work when it is no more alive.
     *
     * @param actors                 set of actors to wait upon
     * @param intervalInMilliseconds interval in milliseconds between checking if the actor is alive
     * @param <T>                    type of the actor extending actor class
     */
    <T extends Actor<E>> void awaitCompletion(@NotNull Set<T> actors, int intervalInMilliseconds);
}
