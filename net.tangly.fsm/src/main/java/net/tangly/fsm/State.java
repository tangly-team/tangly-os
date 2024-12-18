/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.fsm;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * The state abstraction of a hierarchical finite state machine.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public interface State<O, S extends Enum<S>, E extends Enum<E>> extends Comparable<State<O, S, E>> {
    @Override
    default int compareTo(@NotNull State<O, S, E> other) {
        return id().compareTo(other.id());
    }

    /**
     * Gets the id of this state.
     *
     * @return the id of this state.
     */
    S id();

    /**
     * Return true if the state has history active.
     *
     * @return flag indicating if the state has history
     */
    boolean hasHistory();

    /**
     * Return true if the state is an initial state.
     *
     * @return flag indicating if the state is an initial state
     */
    boolean isInitial();

    /**
     * Return true if the state is a final state. A state is final if no transitions are leaving it.
     *
     * @return flag indicating if the state is final
     */
    default boolean isFinal() {
        return localTransitions().isEmpty() && transitions().isEmpty();
    }

    /**
     * Return true if the state contains substates. If true, the state cannot be initial or final.
     *
     * @return flag indicating if the state is composite.
     */
    default boolean isComposite() {
        return !substates().isEmpty();
    }

    /**
     * Execute the entry action associated with the state if defined.
     *
     * @param owner instance owning the finite state machine
     * @param event the event triggering the execution of the action if defined
     */
    void executeEntryAction(O owner, Event<E> event);

    /**
     * Execute the exit action associated with the state if defined.
     *
     * @param owner instance owning the finite state machine
     * @param event the event triggering the execution of the action if defined
     */
    void executeExitAction(O owner, Event<E> event);

    /**
     * Return the list of substates.
     *
     * @return the set of substates or an empty list if none is defined.
     */
    Set<State<O, S, E>> substates();

    /**
     * Return the list of transitions starting from this state.
     *
     * @return the set of transitions or an empty list if none is defined
     */
    Set<Transition<O, S, E>> transitions();

    /**
     * Return the list of local or self transitions starting from this state. Self-transitions do not trigger entry or exit actions.
     *
     * @return the set of self-transitions or an empty list if none is defined
     */
    Set<Transition<O, S, E>> localTransitions();

    /**
     * Return true if the state has an entry action.
     *
     * @return flag indicating if the state has an entry action
     */
    default boolean hasEntryAction() {
        return Objects.nonNull(entryAction());
    }

    /**
     * Return the entry action of the state.
     *
     * @return the action if defined otherwise null
     */
    BiConsumer<O, Event<E>> entryAction();

    /**
     * Return true if the state has an exit action.
     *
     * @return flag indicating if the state has an exit action
     */
    default boolean hasExitAction() {
        return Objects.nonNull(entryAction());
    }

    /**
     * Return the exit action of the state.
     *
     * @return the action if defined otherwise null
     */
    BiConsumer<O, Event<E>> exitAction();

    /**
     * Return the state with the given state identifier.
     *
     * @param stateId identifier of the state looked for
     * @return the requested state if found otherwise null
     */
    State<O, S, E> getStateFor(S stateId);

    /**
     * Return the direct substate being an initial state.
     *
     * @return the direct substate flagged with initial otherwise null
     */
    State<O, S, E> initialState();

    /**
     * Return the state machine's default initial state.
     *
     * @return the ordered hierarchy of states being an initial state if defined otherwise an empty list. The head contains the root state, the tail contains
     * the deepest substate
     */
    Deque<State<O, S, E>> initialStates();

    /**
     * Return the human-readable description of the state.
     *
     * @return the description of the state if defined otherwise null
     */
    String description();

    /**
     * Return the human-readable description for the entry action of the state.
     *
     * @return the description of the entry action for the state if defined otherwise null
     */
    String entryActionDescription();

    /**
     * Return the human-readable description for the exit action of the state.
     *
     * @return the description of the exit action for state if defined otherwise null
     */
    String exitActionDescription();

    /**
     * Return the list of all substates including this state and the given substate to reach the given substate. The hierarchy includes the boundary states. If not found, the list
     * is empty.
     *
     * @param substate substate to look for
     * @return the ordered hierarchy of states to reach the substate if found otherwise an empty list. The head contains the root state, the tail contains the searched substate
     */
    default Deque<State<O, S, E>> getHierarchyFor(State<O, S, E> substate) {
        return getHierarchyFor(new ArrayDeque<>(), this, substate);
    }

    /**
     * Return the state with the given identifier.
     *
     * @param stateId identifier of the state to be found
     * @return the state if found
     */
    default Optional<State<O, S, E>> findBy(@NotNull S stateId) {
        Optional<State<O, S, E>> result = Optional.empty();
        if (id() == stateId) {
            result = Optional.of(this);
        } else {
            for (State<O, S, E> state : substates()) {
                result = state.findBy(stateId);
                if (result.isPresent()) {
                    break;
                }
            }
        }
        return result;
    }

    private Deque<State<O, S, E>> getHierarchyFor(Deque<State<O, S, E>> substates, State<O, S, E> compositeState, State<O, S, E> substate) {
        if (compositeState.substates().contains(substate)) {
            substates.addFirst(substate);
            substates.addFirst(this);
        } else {
            for (var state : compositeState.substates()) {
                if (!state.getHierarchyFor(substates, state, substate).isEmpty()) {
                    substates.addFirst(compositeState);
                    break;
                }
            }
        }
        return substates;
    }
}
