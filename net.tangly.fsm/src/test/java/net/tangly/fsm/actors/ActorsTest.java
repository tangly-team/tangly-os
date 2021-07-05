/*
 * Copyright 2021 Marcel Baumann
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ActorsTest {
    static record Message(String command, Integer payload) {
    }

    static class PeerActor extends ActorImp<ActorsTest.Message> implements Actor<Message> {
        private PeerActor peer;
        private int counter;

        public PeerActor(String name) {
            super(name);
        }

        public void peer(PeerActor peer) {
            this.peer = peer;
        }

        @Override
        public void run() {
            for (; ; ) {
                Message msg = message();
                if (msg.payload() < 20) {
                    counter++;
                    peer.receive(new Message(msg.command, msg.payload + 1));
                }
            }
        }
    }

    static class RegisteredActor extends ActorImp<ActorsTest.Message> implements Actor<Message> {
        private final String peerName;
        private final Actors<Message> actors;
        private int counter;

        public RegisteredActor(String name, Actors<Message> actors, String peerName) {
            super(name);
            this.actors = actors;
            this.peerName = peerName;
        }


        @Override
        public void run() {
            for (; ; ) {
                Message msg = message();
                if (msg.payload() < 20) {
                    counter++;
                    actors.sendMsgTo(new Message(msg.command, msg.payload + 1), actors.actorNamed(peerName).get().id());
                }
            }
        }
    }

    @Test
    void runPeersStandalone() {
        ExecutorService service = Executors.newCachedThreadPool();
        PeerActor one = new PeerActor("One");
        PeerActor two = new PeerActor("Two");
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
    void runPeersWithActors() {
        ExecutorService service = Executors.newCachedThreadPool();
        Actors<Message> actors = new ActorsImp<>(service);
        actors.register(new RegisteredActor("one", actors, "two"));
        actors.register(new RegisteredActor("two", actors, "one"));

        actors.actorNamed("one").get().receive(new Message("do", 0));

        Actors.awaitTermination(service, 1, TimeUnit.SECONDS);
        assertThat(((RegisteredActor) actors.actorNamed("one").get()).counter).isEqualTo(10);
        assertThat(((RegisteredActor) actors.actorNamed("two").get()).counter).isEqualTo(10);
    }
}
