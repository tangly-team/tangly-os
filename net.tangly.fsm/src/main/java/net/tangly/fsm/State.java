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

package net.tangly.fsm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

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
     * Returns true if the state has history active.
     *
     * @return flag indicating if the state has history
     */
    boolean hasHistory();

    /**
     * Returns true if the state is an initial state.
     *
     * @return flag indicating if the sate is an initial one
     */
    boolean isInitial();

    /**
     * Returns true if the state is a final state. A state is final if no transitions are leaving it.
     *
     * @return flag indicating if the state is a final one
     */
    default boolean isFinal() {
        return localTransitions().isEmpty() && transitions().isEmpty();
    }

    /**
     * Returns true if the state contains substates. If true the state cannot be initial or final.
     *
     * @return flag indicating if the state is a composite one.
     */
    default boolean isComposite() {
        return !substates().isEmpty();
    }

    /**
     * Executes the entry action associated with the state if defined.
     *
     * @param owner instance owning the finite state machine
     * @param event the event triggering the execution of the action if defined
     */
    void executeEntryAction(O owner, Event<E> event);

    /**
     * Executes the exit action associated with the state if defined.
     *
     * @param owner instance owning the finite state machine
     * @param event the event triggering the execution of the action if defined
     */
    void executeExitAction(O owner, Event<E> event);

    /**
     * Returns the list of substates.
     *
     * @return the list of substates or an empty list if none is defined.
     */
    Set<State<O, S, E>> substates();

    /**
     * Returns the list of transitions starting from this state.
     *
     * @return the list of transitions or an empty list if none is defined
     */
    Set<Transition<O, S, E>> transitions();

    /**
     * Returns the list of local or self transitions starting from this state. Self transitions do not trigger entry or exit actions.
     *
     * @return the list of self transitions or an empty list if none is defined
     */
    Set<Transition<O, S, E>> localTransitions();

    /**
     * Returns true if the state has an entry action.
     *
     * @return flag indicating if the sate has an entry action
     */
    default boolean hasEntryAction() {
        return Objects.nonNull(entryAction());
    }

    /**
     * Returns the entry action of the state.
     *
     * @return the action if defined otherwise null
     */
    BiConsumer<O, Event<E>> entryAction();

    /**
     * Returns true if the state has an exit action.
     *
     * @return flag indicating if the sate has an exit action
     */
    default boolean hasExitAction() {
        return Objects.nonNull(entryAction());
    }

    /**
     * Returns the exit action of the state.
     *
     * @return the action if defined otherwise null
     */
    BiConsumer<O, Event<E>> exitAction();

    /**
     * Returns the state with the given state identifier.
     *
     * @param stateId identifier of the state looked for
     * @return the requested state if found otherwise null
     */
    State<O, S, E> getStateFor(S stateId);

    /**
     * Returns the direct substate being an initial state.
     *
     * @return the direct substate flagged with initial otherwise null
     */
    State<O, S, E> initialState();

    /**
     * Returns the state machine's default initial state.
     *
     * @return the ordered hierarchy of states being an initial state if defined otherwise an empty list. The head contains the root state, the tail contains
     * the deepest substate
     */
    Deque<State<O, S, E>> initialStates();

    /**
     * Returns the human readable description of the state.
     *
     * @return the description of the state if defined otherwise null
     */
    String description();

    /**
     * Returns the human readable description of the entry action of the state.
     *
     * @return the description of the entry action of state if defined otherwise null
     */
    String entryActionDescription();

    /**
     * Returns the human readable description of the exit action of the state.
     *
     * @return the description of the exit action of state if defined otherwise null
     */
    String exitActionDescription();

    /**
     * Returns the list of all substates including this state and the given substate to reach the given substate. The hierarchy includes the boundary states. If
     * not found the list is empty.
     *
     * @param substate substate to look for
     * @return the ordered hierarchy of states to reach the substate if found otherwise an empty list. The head contains the root state, the tail contains the
     * searched substate
     */
    default Deque<State<O, S, E>> getHierarchyFor(State<O, S, E> substate) {
        return getHierarchyFor(new ArrayDeque<>(), this, substate);
    }

    /**
     * Returns the state with the given identifier.
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
