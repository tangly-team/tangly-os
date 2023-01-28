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

package net.tangly.fsm.actors;

import net.tangly.fsm.Event;
import net.tangly.fsm.dsl.FsmBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The finite state machine internal states for the client test configuration.
 */
enum ClientStates {
    Root, Idle, WaitingForResponse, Finished
}

/**
 * The finite state machine internal states for the server test configuration.
 */
enum ServerStates {
    Root, WaitingForRequest
}

/**
 * The finite state machine events for the test configuration.
 */
enum Events {
    Inquiry, Request, Response
}

class Server extends ActorFsm<Server, ServerStates, Events> {

    int nrRequests;
    private final Actors<Event<Events>> actors;


    private static FsmBuilder<Server, ServerStates, Events> buildFsm() {
        FsmBuilder<Server, ServerStates, Events> builder = FsmBuilder.of(ServerStates.Root);
        builder.root().add(ServerStates.WaitingForRequest).isInitial();
        builder.in(ServerStates.WaitingForRequest).onLocal(Events.Request).execute(Server::processRequest).build();
        return builder;
    }

    Server(String name, Actors<Event<Events>> actors) {
        super(buildFsm(), name);
        this.actors = actors;
    }

    private void processRequest(Event<Events> event) {
        nrRequests++;
        var clientName = (String) (event.parameters().get(0));
        actors.sendMsgTo(Event.of(Events.Response, name(), clientName), actors.actorNamed(clientName).get().id());
    }
}

class Client extends ActorFsm<Client, ClientStates, Events> {

    int nrRequests;
    int nrAnswers;
    private final Actors<Event<Events>> actors;

    private static FsmBuilder<Client, ClientStates, Events> buildFsm() {
        FsmBuilder<Client, ClientStates, Events> builder = FsmBuilder.of(ClientStates.Root);
        builder.root().add(ClientStates.Idle).isInitial();
        builder.root().add(ClientStates.WaitingForResponse);
        builder.root().add(ClientStates.Finished);
        builder.in(ClientStates.Idle).on(Events.Inquiry).to(ClientStates.WaitingForResponse).execute(Client::sendRequestToServer).build();
        builder.in(ClientStates.WaitingForResponse).on(Events.Response).to(ClientStates.Finished).execute(Client::processAnswer).build();
        return builder;
    }

    Client(String name, Actors<Event<Events>> actors) {
        super(buildFsm(), name);
        this.actors = actors;
    }

    private void sendRequestToServer(Event<Events> event) {
        var serverName = (String) (event.parameters().get(1));
        actors.sendMsgTo(Event.of(Events.Request, name(), serverName), actors.actorNamed(serverName).get().id());
        nrRequests++;
    }

    private void processAnswer() {
        nrAnswers++;
    }
}

/**
 * The class shows how to implement a finite state machine context using the quasar library to exchange events between active state machine instances.
 */
class ActorTest {
    static final String SERVER = "server";
    static final String CLIENT = "client";

    /**
     * Sends a request from a client to the server and waits for the answer.
     */
    @Test
    void activeFsmTest() {
        ExecutorService service = Executors.newCachedThreadPool();
        Actors<Event<Events>> actors = new ActorsImp<>(service);
        actors.register(new Server(SERVER, actors));
        actors.register(new Client(CLIENT, actors));
        actors.actorNamed(CLIENT).get().receive(new Event<>(Events.Inquiry, List.of(CLIENT, SERVER)));

        Actors.awaitTermination(service, 10, TimeUnit.SECONDS);
        assertThat(((Client) actors.actorNamed(CLIENT).get()).nrRequests).isEqualTo(1);
        assertThat(((Client) actors.actorNamed(CLIENT).get()).nrAnswers).isEqualTo(1);
        assertThat(((Server) actors.actorNamed(SERVER).get()).nrRequests).isEqualTo(1);
    }

    /**
     * Sends a request from a thousand clients to one server and waits for all answers.
     */
    @Test
    void activeMultipleFsmTest() {
        ExecutorService service = Executors.newCachedThreadPool();
        Actors<Event<Events>> actors = new ActorsImp<>(service);
        final int NR_CLIENTS = 10_000;
        final String CLIENT_PREFIX = "client-";
        actors.register(new Server(SERVER, actors));
        for (int i = 0; i < NR_CLIENTS; i++) {
            String name = CLIENT_PREFIX + i;
            actors.register(new Client(name, actors));
            actors.actorNamed(name).get().receive(new Event<>(Events.Inquiry, List.of(name, SERVER)));
        }
        Actors.awaitTermination(service, 1, TimeUnit.SECONDS);
        for (int i = 0; i < NR_CLIENTS; i++) {
            Client client = (Client) actors.actorNamed(CLIENT_PREFIX + i).get();
            assertThat(client.nrRequests).isEqualTo(1);
            assertThat(client.nrAnswers).isEqualTo(1);
        }
        assertThat(((Server) actors.actorNamed(SERVER).get()).nrRequests).isEqualTo(NR_CLIENTS);
    }
}
