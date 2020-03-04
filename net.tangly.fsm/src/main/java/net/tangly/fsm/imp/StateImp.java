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

package net.tangly.fsm.imp;

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.Transition;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Default implementation of an mutable class providing the state immutable interface.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
class StateImp<O, S extends Enum<S>, E extends Enum<E>> implements State<O, S, E> {

    private final S id;
    private boolean hasHistory;
    private boolean initial;
    private BiConsumer<O, Event<E>> entryAction;
    private BiConsumer<O, Event<E>> exitAction;
    private final Set<State<O, S, E>> substates;
    private final Set<Transition<O, S, E>> localTransitions;
    private final Set<Transition<O, S, E>> transitions;

    private String description;
    private String entryActionDescription;
    private String exitActionDescription;

    /**
     * Default constructor of the class.
     *
     * @param id identifier of the state
     */
    StateImp(S id) {
        this.id = id;
        substates = new HashSet<>();
        localTransitions = new HashSet<>();
        transitions = new HashSet<>();
    }

    @Override
    public S id() {
        return id;
    }

    @Override
    public boolean hasHistory() {
        return hasHistory;
    }

    /**
     * Sets the history flag of the state.
     *
     * @param hasHistory new value of the flag
     * @see #hasHistory()
     */
    void setHasHistory(boolean hasHistory) {
        this.hasHistory = hasHistory;
    }

    @Override
    public boolean isInitial() {
        return initial;
    }

    /**
     * Sets the initial flag of the state.
     *
     * @param initial new value of the flag
     * @see #isInitial()
     */
    void setInitial(boolean initial) {
        this.initial = initial;
    }

    @Override
    public void executeEntryAction(O owner, Event<E> event) {
        if (entryAction != null) {
            entryAction.accept(owner, event);
        }
    }

    @Override
    public void executeExitAction(O owner, Event<E> event) {
        if (exitAction != null) {
            exitAction.accept(owner, event);
        }
    }

    /**
     * Sets the entry action of the state.
     *
     * @param entryAction new entry action of the state
     * @see #entryAction()
     */
    void setEntryAction(BiConsumer<O, Event<E>> entryAction) {
        this.entryAction = entryAction;
    }

    @Override
    public BiConsumer<O, Event<E>> entryAction() {
        return entryAction;
    }


    /**
     * Sets the exit action of the state.
     *
     * @param exitAction new entry action of the state
     * @see #exitAction()
     */
    void setExitAction(BiConsumer<O, Event<E>> exitAction) {
        this.exitAction = exitAction;
    }

    public BiConsumer<O, Event<E>> exitAction() {
        return exitAction;
    }

    @Override
    public Set<State<O, S, E>> substates() {
        return Collections.unmodifiableSet(substates);
    }

    /**
     * Adds a new substate to this composite state.
     *
     * @param substate new substate to addToRoot
     * @see #substates()
     */
    void addSubstate(State<O, S, E> substate) {
        substates.add(substate);
    }

    @Override
    public Set<Transition<O, S, E>> transitions() {
        return Collections.unmodifiableSet(transitions);
    }

    /**
     * Adds a new transition to this state.
     *
     * @param transition new transition to addToRoot
     * @see #transitions()
     */
    void addTransition(Transition<O, S, E> transition) {
        transitions.add(transition);
    }

    @Override
    public Set<Transition<O, S, E>> localTransitions() {
        return Collections.unmodifiableSet(localTransitions);
    }

    /**
     * Adds a new local transition to this state.
     *
     * @param transition new local transition to addToRoot
     * @see #localTransitions()
     */
    void addLocalTransition(Transition<O, S, E> transition) {
        localTransitions.add(transition);
    }

    @Override
    public String description() {
        return description;
    }

    /**
     * Sets the description of the state.
     *
     * @param description new description of the state
     * @see #description()
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String entryActionDescription() {
        return entryActionDescription;
    }

    /**
     * Sets the description of the entry action.
     *
     * @param description new description of the entry action
     * @see #entryActionDescription()
     */
    void setEntryActionDescription(String description) {
        this.entryActionDescription = description;
    }

    @Override
    public String exitActionDescription() {
        return exitActionDescription;
    }

    /**
     * Sets the description of the exit action.
     *
     * @param description new description of the exit action
     * @see #exitActionDescription()
     */
    void setExitActionDescription(String description) {
        this.exitActionDescription = description;
    }

    @Override
    public State<O, S, E> initialState() {
        return substates().stream().filter(State::isInitial).findAny().orElse(null);
    }

    @Override
    public Deque<State<O, S, E>> initialStates() {
        Deque<State<O, S, E>> states = new ArrayDeque<>();
        if (isInitial()) {
            states.addLast(this);
            State<O, S, E> initialState = initialState();
            while (initialState != null) {
                states.addLast(initialState);
                initialState = initialState.initialState();
            }
        }
        return states;
    }

    @Override
    public State<O, S, E> getStateFor(S stateId) {
        if (this.id == stateId) {
            return this;
        } else {
            State<O, S, E> substate;
            for (var candidate : this.substates()) {
                substate = candidate.getStateFor(stateId);
                if (substate != null) {
                    return substate;
                }
            }
        }
        return null;
    }

    @Override
    public Deque<State<O, S, E>> getHierarchyFor(State<O, S, E> substate) {
        return getHierarchyFor(new ArrayDeque<>(), this, substate);
    }

    @Override
    public String toString() {
        return String.format("id=%s, hasHistory=%s, initial=%s, description=%s, entryAction=%s, exitAction=%s", id, hasHistory, initial, description,
                entryActionDescription, exitActionDescription);
    }

    private Deque<State<O, S, E>> getHierarchyFor(Deque<State<O, S, E>> substates, State<O, S, E> compositeState, State<O, S, E> substate) {
        if (compositeState.substates().contains(substate)) {
            substates.addFirst(substate);
            substates.addFirst(this);
        } else {
            for (var state : compositeState.substates()) {
                if (!((StateImp<O, S, E>) state).getHierarchyFor(substates, state, substate).isEmpty()) {
                    substates.addFirst(compositeState);
                    break;
                }
            }
        }
        return substates;
    }
}
