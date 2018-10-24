/*
 *
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.fsm.dsl;

/**
 * Domain specific language fluent interface to configure a new transition to another node.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public interface ToTransitionBuilder<O, S extends Enum<?>, E extends Enum<E>> {

    /**
     * Creates a transition from the selected state to the one with the given identifier. A transition
     * builder is returned.
     *
     * @param stateId identifier of the state where the transition arrives
     * @return transition builder to configure the transition
     */
    default TransitionBuilder<O, S, E> to(S stateId) {
        return to(stateId, null);
    }

    /**
     * Creates a transition from the selected state to the one with the given identifier. A transition
     * builder is returned.
     *
     * @param stateId     identifier of the state where the transition arrives
     * @param description description of the transition
     * @return transition builder to configure the transition
     */
    TransitionBuilder<O, S, E> to(S stateId, String description);
}