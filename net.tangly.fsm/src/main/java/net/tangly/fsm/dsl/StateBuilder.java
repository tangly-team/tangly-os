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

package net.tangly.fsm.dsl;

/**
 * Domain specific language fluent interface to configure an existing state with new .
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public interface StateBuilder<O, S extends Enum<?>, E extends Enum<E>> {

    /**
     * Once a state is selected, you can addToRoot a new substate.
     *
     * @param stateId the identifier of the substate to addToRoot
     * @return the substate builder to complete the creation and configuration of a substate
     */
    default SubStateBuilder<O, S, E> add(S stateId) {
        return add(stateId, null);
    }

    /**
     * Once a state is selected, you can addToRoot a new substate.
     *
     * @param stateId     the identifier of the substate to addToRoot
     * @param description description of the substate
     * @return the substate builder to complete the creation and configuration of a substate
     */
    SubStateBuilder<O, S, E> add(S stateId, String description);

    /**
     * Once a state is selected, you can addToRoot a new transition leaving this state.
     *
     * @param eventId the identifier of the event firing the transition leaving the state
     * @return the transition builder to complete the creation and configuration of a transition
     */
    ToTransitionBuilder<O, S, E> on(E eventId);

    /**
     * Once a state is selected, you can addToRoot a new local transition on the state.
     *
     * @param eventId the identifier of the event firing the local transition on the state
     * @return the transition builder to complete the creation and configuration of a transition
     */
    default TransitionBuilder<O, S, E> onLocal(E eventId) {
        return onLocal(eventId, null);
    }

    /**
     * Once a state is selected, you can addToRoot a new local transition on the state.
     *
     * @param eventId     the identifier of the event firing the local transition on the state
     * @param description description of the substate
     * @return the transition builder to complete the creation and configuration of a transition
     */
    TransitionBuilder<O, S, E> onLocal(E eventId, String description);
}
