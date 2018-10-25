/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.fsm;

import java.util.function.BiConsumer;

/**
 * The handler observing all the changes in a hierarchical final state machine. Enough information
 * is provided to implement sophisticated mechanisms such as tracing, profiling or validating FSM
 * during execution. All methods are defined as default ones to ease the creation of custom
 * handlers.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public interface StateMachineEventHandler<O, S extends Enum<S>, E extends Enum<E>> {

    /**
     * Callback before the processing of an event is started.
     *
     * @param event event being processed
     */
    default void processEvent(Event<E> event) {
    }

    /**
     * The reset operation was called on the state machine.
     */
    default void wasReset() {
    }

    /**
     * Callback before the transition is fired.
     *
     * @param transition transition being fired.
     * @param event      event triggering the evaluation
     */
    default void fireTransition(Transition<O, S, E> transition, Event<E> event) {
    }

    /**
     * Callback before the action of the transition is processed.
     *
     * @param transition transition which action will be executed
     * @param event      event triggering the evaluation
     */
    default void fireLocalTransition(Transition<O, S, E> transition, Event<E> event) {
    }

    /**
     * Callback before the entry action of the state is processed.
     *
     * @param state state which entry action will be executed
     * @param event event triggering the evaluation
     */
    default void executeEntryAction(State<O, S, E> state, Event<E> event) {
    }

    /**
     * Callback before the exit action of the state is processed.
     *
     * @param state state which exit action will be executed
     * @param event event triggering the evaluation
     */
    default void executeExitAction(State<O, S, E> state, Event<E> event) {
    }

    /**
     * Callback before the state is entered.
     *
     * @param state state being entered
     */
    default void enterState(State<O, S, E> state) {
    }

    /**
     * Callback before the state is left.
     *
     * @param state state being left
     */
    default void exitState(State<O, S, E> state) {
    }

    /**
     * Callback when an exception was thrown during the evaluation of a transition guard or action.
     *
     * @param transition transition being fired
     * @param event      event triggering the evaluation
     * @param e          exception caught by the finite state machine.
     */
    default void throwException(Transition<O, S, E> transition, Event<E> event, Exception e) {
    }


    /**
     * Callback when an exception was thrown during the execution of an entry state action.
     *
     * @param state  state being processed
     * @param action action throwing the exception
     * @param event  event triggering the evaluation
     * @param e      exception caught by the finite state machine.
     */
    default void throwException(State<O, S, E> state, BiConsumer<O, Event<E>> action, Event<E> event, Exception e) {
    }
}
