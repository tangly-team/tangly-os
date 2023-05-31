/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.fsm.actors;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The actor infrastructure enabler provides asynchronous execution for actors he is in charge.
 * <p>An actor shall be associated with a virtual Java thread. Most of the time an actor is waiting on the next message to process.
 * This topology is an ideal case for virtual threads. Two approaches are supported.</p>
 * <ul>
 *     <li>Actor instances are registered to the actors registry. The registry provides helper methods to send a message to an actor based on its identity. Naturally the
 *     developer can directly send a message to an actor instance if preferred./li>
 *     <li>Channels are registered to the actors library. Actors can register as providers or consumers of a channel instance. The registry provides helper methods to publish a
 *     message on a channel. Conceptually a channel is publish and subscribe topic. A channel can have multiple publishers and consumers.</li>
 * </ul>
 *
 * @param <T> type of the messages exchanged between a set of actors
 */
public class Actors<T> {
    private final Map<UUID, Actor<T>> actors;
    private final Map<String, Channel<T>> channels;
    private final ExecutorService executor;

    static void awaitTermination(ExecutorService service, long timeout, @NotNull TimeUnit unit) {
        try {
            service.awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default constructor.
     */
    public Actors() {
        this(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Constructor with a customer executor.
     *
     * @param executor executor to use when activating actors
     */
    public Actors(@NotNull ExecutorService executor) {
        actors = new ConcurrentHashMap<>();
        channels = new ConcurrentHashMap<>();
        this.executor = executor;
    }

    /**
     * Send an event to the actor with the given name.
     *
     * @param message message to send
     * @param id      identifier of the actor
     */
    public void sendMsgTo(@NotNull T message, @NotNull UUID id) {
        if (actors.containsKey(id)) {
            actors.get(id).receive(message);
        }
    }

    /**
     * Send a message through the channel with the given name. If no channel could be found, the request is discarded.
     *
     * @param message message sent on the channel
     * @param channel external identifier of the channel to use
     */
    void send(@NotNull T message, @NotNull String channel) {
        var handler = channelNamed(channel);
        handler.ifPresent(o -> o.publisher().submit(message));
    }

    /**
     * Return an actor with the given name.
     *
     * @param name name of the actor to be found
     * @return the requested actor if found
     */
    public Optional<Actor<T>> actorNamed(@NotNull String name) {
        return actors.values().stream().filter(o -> name.equals(o.name())).findAny();
    }

    /**
     * Return the channel with the given name.
     *
     * @param name name of the channel to be found
     * @return the requested channel if found
     */
    public Optional<Channel<T>> channelNamed(@NotNull String name) {
        return Optional.ofNullable(channels.get(name));
    }

    /**
     * Register the actor in the pool of known actors and activate it by submitting it to the executor.
     *
     * @param actor actor to register
     */
    public void register(@NotNull Actor<T> actor) {
        actors.put(actor.id(), actor);
        executor.submit(actor);
    }

    /**
     * Register the actor in the pool of known actors and activate it by submitting it to the executor. Upon activation, the actor subscribes to the channel.
     *
     * @param actor   actor to register
     * @param channel channel to subscribe to
     */
    public void register(@NotNull Actor<T> actor, @NotNull String channel) {
        Objects.nonNull(channels.get(channel));
        register(actor);
        channels.get(channel).subscribe(actor);
    }

    /**
     * Subscribe the actor to the channel.
     *
     * @param actorId identifier of the actor as registered
     * @param channel name of the channel as registered
     */
    void subscribeTo(@NotNull UUID actorId, @NotNull String channel) {
        Objects.nonNull(channels.get(channel));
        Objects.nonNull(actors.get(actorId));
        channels.get(channel).subscribe(actors.get(actorId));
    }

    /**
     * Register the channel and activate it.
     *
     * @param channel
     */
    public void register(@NotNull String channel) {
        channels.put(channel, new Channel<>(channel));
    }

    public void awaitTermination(long timeout, @NotNull TimeUnit unit) {
        Actors.awaitTermination(executor, timeout, unit);
    }
}
