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

package net.tangly.fsm.imp;

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.StateMachineEventHandler;
import net.tangly.fsm.Transition;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Helper class to manage a set of state machine event handler implemented as a classical facade.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
class StateMachineEventHandlerHelper<O, S extends Enum<S>, E extends Enum<E>> implements StateMachineEventHandler<O, S, E> {

    private final Set<StateMachineEventHandler<O, S, E>> handlers;

    /**
     * Default constructor of the class.
     */
    StateMachineEventHandlerHelper() {
        handlers = new HashSet<>();
    }

    /**
     * adds an event handler in the managed set.
     *
     * @param handler handler to addToRoot to the managed set.
     */
    void addEventHandler(StateMachineEventHandler<O, S, E> handler) {
        handlers.add(handler);
    }

    /**
     * removes an event handler from the managed set.
     *
     * @param handler handler to remove from the managed set.
     */
    void removeEventHandler(StateMachineEventHandler<O, S, E> handler) {
        handlers.remove(handler);
    }

    /**
     * Returns true if the given handler is registered in the state machine.
     *
     * @param handler handler whose registration shall be verified
     * @return true if the handler is registered otherwise false
     */
    boolean isRegistered(StateMachineEventHandler<O, S, E> handler) {
        return handlers.contains(handler);
    }

    @Override
    public void processEvent(Event<E> event) {
        handlers.forEach(handler -> handler.processEvent(event));
    }

    @Override
    public void wasReset() {
        handlers.forEach(StateMachineEventHandler::wasReset);
    }

    @Override
    public void fireLocalTransition(Transition<O, S, E> transition, Event<E> event) {
        handlers.forEach(handler -> handler.fireLocalTransition(transition, event));
    }

    @Override
    public void fireTransition(Transition<O, S, E> transition, Event<E> event) {
        handlers.forEach(handler -> handler.fireTransition(transition, event));
    }

    @Override
    public void executeEntryAction(State<O, S, E> state, Event<E> event) {
        handlers.forEach(handler -> handler.executeEntryAction(state, event));
    }

    @Override
    public void executeExitAction(State<O, S, E> state, Event<E> event) {
        handlers.forEach(handler -> handler.executeExitAction(state, event));
    }

    @Override
    public void enterState(State<O, S, E> state) {
        handlers.forEach(handler -> handler.enterState(state));
    }

    @Override
    public void exitState(State<O, S, E> state) {
        handlers.forEach(handler -> handler.exitState(state));
    }

    @Override
    public void throwException(Transition<O, S, E> transition, Event<E> event, Exception e) {
        handlers.forEach(handler -> handler.throwException(transition, event, e));
    }

    @Override
    public void throwException(State<O, S, E> state, BiConsumer<O, Event<E>> action, Event<E> event, Exception e) {
        handlers.forEach(handler -> handler.throwException(state, action, event, e));
    }
}
