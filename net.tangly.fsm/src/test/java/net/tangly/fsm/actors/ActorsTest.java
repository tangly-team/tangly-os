/*
 * Copyright 2021-2024 Marcel Baumann
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

package net.tangly.fsm.actors;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ActorsTest {
    static final String ONE = "one";
    static final String TWO = "two";
    static final String CHANNEL = "channel";

    record Message(String command, Integer payload) {
    }

    static class PeerActor extends ActorImp<Message> implements Actor<Message> {
        private PeerActor peer;
        private int counter;

        PeerActor(String name, ExecutorService executor) {
            super(name, executor);
        }

        void peer(PeerActor peer) {
            this.peer = peer;
        }

        @Override
        protected boolean process(@NotNull Message msg) {
            if (msg.payload() < 20) {
                counter++;
                peer.receive(new Message(msg.command, msg.payload + 1));
                return true;
            } else {
                return false;
            }
        }
    }

    static class RegisteredActor extends ActorImp<Message> implements Actor<Message> {
        private final String peerName;
        private final Actors<Message> actors;
        private int counter;

        RegisteredActor(String name, Actors<Message> actors, String peerName) {
            super(name, actors.executor());
            this.actors = actors;
            this.peerName = peerName;
            actors.register(this);
        }

        @Override
        protected boolean process(@NotNull Message msg) {
            if (msg.payload() < 20) {
                counter++;
                actors.sendTo(new Message(msg.command, msg.payload + 1), actors.actorNamed(peerName).map(Actor::id).orElseThrow());
                return true;
            } else {
                return false;
            }
        }
    }

    static class FlowActor extends ActorImp<Message> implements Actor<Message> {
        private int counter;

        FlowActor(String name, Actors<Message> actors) {
            super(name, actors.executor());
        }

        @Override
        protected boolean process(@NotNull Message msg) {
            if (counter < 20) {
                counter++;
                return true;
            } else {
                return false;
            }
        }
    }

    static class SingleRegisterActor extends ActorImp<Message> implements Actor<Message> {
        private final Actors actors;
        private final String channel;
        private int counter;

        SingleRegisterActor(String name, Actors<Message> actors, String channel) {
            super(name, actors.executor());
            this.actors = actors;
            this.channel = channel;
            actors.register(this);
        }

        @Override
        protected boolean process(@NotNull Message msg) {
            if (counter < 20) {
                ++counter;
                if (Objects.isNull(channel)) {
                    actors.sendTo(msg, id());
                } else {
                    actors.publish(msg, channel);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Test
    void runWithStandaloneActors() {
        ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
        PeerActor one = new PeerActor("One", service);
        PeerActor two = new PeerActor("Two", service);
        one.peer(two);
        two.peer(one);
        service.submit(one);
        service.submit(two);

        one.receive(new Message("do", 0));
        Actors.awaitTermination(service, 1, TimeUnit.SECONDS);

        assertThat(one.counter).isEqualTo(10);
        assertThat(two.counter).isEqualTo(10);
    }

    @Test
    void runSingleActorWithActorRegistry() {
        Actors<Message> actors = new Actors<>();
        new SingleRegisterActor(ONE, actors, null);

        actors.sendTo(new Message("count", 20), actors.actorNamed(ONE).get().id());
        actors.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(((SingleRegisterActor) actors.actorNamed(ONE).get()).counter).isEqualTo(20);
    }

    @Test
    void runSingleActorWithChannelRegistry() {
        Actors<Message> actors = new Actors<>();
        actors.createAndRegister(CHANNEL);
        var actor = new SingleRegisterActor(ONE, actors, CHANNEL);
        actors.register(actor);
        actors.subscribeTo(actor.id(), CHANNEL);

        actors.publish(new Message("count", 20), CHANNEL);
        actors.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(((SingleRegisterActor) actors.actorNamed(ONE).get()).counter).isEqualTo(20);
    }

    @Test
    void runWithActorRegistry() {
        Actors<Message> actors = new Actors<>();
        new RegisteredActor(ONE, actors, TWO);
        new RegisteredActor(TWO, actors, ONE);

        actors.actorNamed("one").ifPresent(o -> o.receive(new Message("do", 0)));
        actors.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(((RegisteredActor) actors.actorNamed(ONE).get()).counter).isEqualTo(10);
        assertThat(((RegisteredActor) actors.actorNamed(TWO).get()).counter).isEqualTo(10);
    }

    @Test
    void runWithChannelRegistry() {
        Actors<Message> actors = new Actors<>();
        actors.createAndRegister(CHANNEL);
        actors.register(new FlowActor(ONE, actors), CHANNEL);
        actors.register(new FlowActor(TWO, actors), CHANNEL);

        IntStream.range(0, 20).forEach(o -> actors.channelNamed(CHANNEL).ifPresent(channel -> channel.publish(new Message("do", o))));
        actors.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(((FlowActor) actors.actorNamed(ONE).get()).counter).isEqualTo(20);
        assertThat(((FlowActor) actors.actorNamed(TWO).get()).counter).isEqualTo(20);
    }
}
