/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.fsm.actor;

import java.util.HashSet;
import java.util.List;

import net.tangly.fsm.Event;
import net.tangly.fsm.actors.Actor;
import net.tangly.fsm.actors.LocalActor;
import net.tangly.fsm.actors.LocalActors;
import net.tangly.fsm.dsl.FsmBuilder;
import org.junit.jupiter.api.Test;

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

class Server extends LocalActor<Server, ServerStates, Events> implements Actor<Events> {

    int nrRequests;

    private static FsmBuilder<Server, ServerStates, Events> buildFsm() {
        FsmBuilder<Server, ServerStates, Events> builder = FsmBuilder.of(ServerStates.Root);
        builder.root().add(ServerStates.WaitingForRequest).isInitial();
        builder.in(ServerStates.WaitingForRequest).onLocal(Events.Request).execute(Server::processRequest);
        return builder;
    }

    Server(String name) {
        super(buildFsm(), name);
    }

    private void processRequest(Event<Events> event) {
        nrRequests++;
        var clientName = (String) (event.parameters().get(0));
        LocalActors.<Events>instance().sendEventTo(Event.of(Events.Response, name(), clientName), clientName);
    }
}

class Client extends LocalActor<Client, ClientStates, Events> implements Actor<Events> {

    int nrRequests;
    int nrAnswers;

    private static FsmBuilder<Client, ClientStates, Events> buildFsm() {
        FsmBuilder<Client, ClientStates, Events> builder = FsmBuilder.of(ClientStates.Root);
        builder.root().add(ClientStates.Idle).isInitial();
        builder.root().add(ClientStates.WaitingForResponse);
        builder.root().add(ClientStates.Finished);
        builder.in(ClientStates.Idle).on(Events.Inquiry).to(ClientStates.WaitingForResponse).execute(Client::sendRequestToServer);
        builder.in(ClientStates.WaitingForResponse).on(Events.Response).to(ClientStates.Finished).execute(Client::processAnswer);
        return builder;
    }

    Client(String name) {
        super(buildFsm(), name);
    }

    private void sendRequestToServer(Event<Events> event) {
        var serverName = (String) (event.parameters().get(1));
        LocalActors.<Events>instance().sendEventTo(Event.of(Events.Request, name(), serverName), serverName);
        nrRequests++;
    }

    private void processAnswer() {
        nrAnswers++;
    }
}

/**
 * The class shows how to implement a finite state machine context using the quasar library to
 * exchange events between active state machine instances.
 */
class ActorTest {
    /**
     * Sends a request from a client to the server and waits for the answer.
     */
    @Test
    void activeFsmTest() {
        var server = new Server("server");
        var client = new Client("client");
        client.receive(new Event<>(Events.Inquiry, List.of("client", "server")));
        LocalActors.<Events>instance().awaitCompletion(client, 1000);
        assertThat(client.nrRequests).isEqualTo(1);
        assertThat(client.nrAnswers).isEqualTo(1);
        assertThat(server.nrRequests).isEqualTo(1);
    }

    /**
     * Sends a request from a thousand clients to one server and waits for all answers.
     */
    @Test
    void activeMultipleFsmTest() {
        final int nrClients = 10_000;
        var server = new Server("server");
        var clients = new HashSet<Client>();
        for (int i = 0; i < nrClients; i++) {
            String name = "client" + i;
            Client client = new Client(name);
            clients.add(client);
            client.receive(new Event<>(Events.Inquiry, List.of(name, "server")));
        }
        LocalActors.<Events>instance().awaitCompletion(clients, 1);
        for (int i = 0; i < nrClients; i++) {
            Client client = LocalActors.<Events>instance().getActorNamed("client" + i);
            assertThat(client.nrRequests).isEqualTo(1);
            assertThat(client.nrAnswers).isEqualTo(1);
        }
        assertThat(server.nrRequests).isEqualTo(nrClients);
    }
}
