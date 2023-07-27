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

package net.tangly.fsm;

import org.jetbrains.annotations.NotNull;

/**
 * The finite state machine executing a hierarchical finite state machine description.
 *
 * @param <O> the class of the instance owning the finite state machine instance or the context in which the machine is executed
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public interface StateMachine<O, S extends Enum<S>, E extends Enum<E>> {

    /**
     * Fire the specified event and processes all associated actions.
     *
     * @param event the event to process
     * @return flag indicating if a transition was fired or not
     */
    boolean fire(@NotNull Event<E> event);

    /**
     * Reset the state machine to his start state. All history information is erased.
     */
    void reset();

    /**
     * Return the name of this state machine.
     *
     * @return name of the state machine if defined otherwise null
     */
    String name();

    /**
     * Return the state machine description.
     *
     * @return the state machine description
     */
    State<O, S, E> root();

    /**
     * Return the context of the finite state machine instance.
     *
     * @return the context of the state machine
     */
    O context();

    /**
     * Return true if the finite state machine is in a non-final state and will process further events.
     *
     * @return flag indicating if the machine is in a non-final state
     */
    boolean isAlive();

    /**
     * Add an event handler.
     *
     * @param handler the event handler
     */
    void addEventHandler(@NotNull StateMachineEventHandler<O, S, E> handler);

    /**
     * Remove the given event handler.
     *
     * @param handler the event handler to be removed.
     */
    void removeEventHandler(@NotNull StateMachineEventHandler<O, S, E> handler);

    /**
     * Return true if the given handler is registered in the state machine.
     *
     * @param handler handler whose registration shall be verified
     * @return true if the handler is registered otherwise false
     */
    boolean isRegistered(@NotNull StateMachineEventHandler<O, S, E> handler);
}
