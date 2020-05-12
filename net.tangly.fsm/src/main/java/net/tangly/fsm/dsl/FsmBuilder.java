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

package net.tangly.fsm.dsl;

import net.tangly.fsm.State;
import net.tangly.fsm.StateMachine;
import net.tangly.fsm.imp.DefinitionBuilder;

/**
 * Domain specific language fluent interface to build a state machine declaration starting with a state. This interface provides the entry point to
 * select a state or add a substate to a selected state.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */

public interface FsmBuilder<O, S extends Enum<S>, E extends Enum<E>> {

    /**
     * Builder method to create a new finite state machine builder.
     *
     * @param rootId the identifier of the root state of the finite state machine to build
     * @param <O>    the class of the instance owning the finite state machine instance
     * @param <S>    enumeration type for the identifiers of states
     * @param <E>    enumeration type for the identifiers of events
     * @return the newly build finite state machine builder
     */
    static <O, S extends Enum<S>, E extends Enum<E>> FsmBuilder<O, S, E> of(S rootId) {
        return new DefinitionBuilder<>(rootId);
    }

    /**
     * Selects the state with the given identifier and returns a state builder.
     *
     * @param stateId identifier of the requested existing state
     * @return the requested state builder
     */
    StateBuilder<O, S, E> in(S stateId);

    /**
     * Selects the root state of the machine and returns a state builder.
     *
     * @return the requested root state builder
     */
    StateBuilder<O, S, E> root();

    /**
     * Returns the root state of the finite state machine definition and terminate the building process.
     *
     * @return the root state of the definition
     */
    State<O, S, E> definition();

    /**
     * Creates an instance of the state machine based on the definition of the state machine available to the builder.
     *
     * @param name  human readable name of the finite state machine instance
     * @param owner instance owning the finite state machine
     * @return the state machine instance
     */
    StateMachine<O, S, E> machine(String name, O owner);

    /**
     * Adds a new substate to the root state.
     *
     * @param stateId the identifier of the substate to addToRoot
     * @return the substate builder to complete the creation and configuration of a substate
     */

    /**
     * Adds a new state to the root state.
     *
     * @param stateId identifier of the new state
     * @return the requested state builder
     */
    default SubStateBuilder<O, S, E> addToRoot(S stateId) {
        return root().add(stateId);
    }

    /**
     * Adds a new state to the root state.
     *
     * @param stateId     identifier of the new state
     * @param description human readable description of the state
     * @return the requested state builder
     */
    default SubStateBuilder<O, S, E> addToRoot(S stateId, String description) {
        return root().add(stateId, description);
    }
}
