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

package net.tangly.fsm.imp;

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.StateMachine;
import net.tangly.fsm.StateMachineEventHandler;
import net.tangly.fsm.Transition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;

/**
 * The implementation of the state machine abstraction.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
class StateMachineImp<O, S extends Enum<S>, E extends Enum<E>> implements StateMachine<O, S, E> {

    /**
     * Human readable name of the state machine instance.
     */
    private final String name;

    /**
     * The root state of the finite state machine description.
     */
    private final State<O, S, E> root;

    /**
     * instance owning the finite state machine instance.
     */
    private final O owner;

    /**
     * Handler helper instance to propagate events generated during processing.
     */
    private final StateMachineEventHandlerHelper<O, S, E> helper;

    /**
     * List of active states, the head contains the root state, the tail contains the deepest state.
     */
    private final Deque<State<O, S, E>> activeStates;

    /**
     * List of all states with active history in the state machine.
     */
    private final Set<State<O, S, E>> history;

    /**
     * Constructor of the class.
     *
     * @param name  human readable name of the finite state machine instance
     * @param root  root state containing the description of the finite state machine
     * @param owner instance owning the finite state machine
     */
    StateMachineImp(String name, @NotNull State<O, S, E> root, O owner) {
        this.name = name;
        this.root = root;
        this.owner = owner;
        activeStates = new ArrayDeque<>();
        history = new HashSet<>();
        helper = new StateMachineEventHandlerHelper<>();
        reset();
    }

    @Override
    public void reset() {
        activeStates.clear();
        history.clear();
        activeStates.add(root);
        enterStatesFromCommonAncestor(null, root.initialStates());
        helper.wasReset();
    }

    @Override
    public boolean fire(Event<E> event) {
        helper.processEvent(event);
        boolean fired = fireLocalTransition(event);
        if (!fired) {
            fired = fireTransition(event);
        }
        return fired;
    }

    private boolean fireLocalTransition(@NotNull Event<E> event) {
        boolean fired = false;
        for (var iter = activeStates.descendingIterator(); iter.hasNext(); ) {
            State<O, S, E> state = iter.next();
            for (var transition : state.localTransitions()) {
                try {
                    if (transition.evaluate(owner, event)) {
                        fired = true;
                        helper.fireLocalTransition(transition, event);
                        fireTransition(transition, event);
                        break;
                    }
                } catch (Exception e) {
                    helper.throwException(transition, event, e);
                }
            }
        }
        return fired;
    }

    private boolean fireTransition(@NotNull Event<E> event) {
        boolean fired = false;
        for (Iterator<State<O, S, E>> iter = activeStates.descendingIterator(); iter.hasNext(); ) {
            var state = iter.next();
            for (var transition : state.transitions()) {
                try {
                    if (transition.evaluate(owner, event)) {
                        fired = true;
                        var hierarchy = getHierarchyUpToCommonAncestor(transition.target());
                        exitStatesToCommonAncestor(event, hierarchy.getFirst());
                        helper.fireTransition(transition, event);
                        fireTransition(transition, event);
                        enterStatesFromCommonAncestor(event, hierarchy);
                        initiateStateWithHistory(event, activeStates.getLast());
                        break;
                    }
                } catch (Exception e) {
                    helper.throwException(transition, event, e);
                }
            }
            if (fired) {
                break;
            }
        }
        return fired;
    }

    private void fireTransition(@NotNull Transition<O, S, E> transition, @NotNull Event<E> event) {
        try {
            transition.execute(owner, event);
        } catch (Exception e) {
            helper.throwException(transition, event, e);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public O context() {
        return owner;
    }

    @Override
    public boolean isAlive() {
        return activeStates.stream().filter(o -> o != root).noneMatch(State::isFinal);
    }

    @Override
    public void addEventHandler(StateMachineEventHandler<O, S, E> handler) {
        helper.addEventHandler(handler);
    }

    @Override
    public void removeEventHandler(StateMachineEventHandler<O, S, E> handler) {
        helper.removeEventHandler(handler);
    }

    @Override
    public boolean isRegistered(StateMachineEventHandler<O, S, E> handler) {
        return helper.isRegistered(handler);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]").add("name=" + name).add("activeStates=" + activeStates)
                .add("history=" + history).toString();
    }

    /**
     * Returns the collection of currently active states.
     *
     * @return the active states unmodifiable collection
     */
    Collection<State<O, S, E>> getActiveStates() {
        return Collections.unmodifiableCollection(activeStates);
    }

    /**
     * Returns the collection of current history states.
     *
     * @return the history states unmodifiable collection
     */
    Collection<State<O, S, E>> getHistoryStates() {
        return Collections.unmodifiableCollection(history);
    }

    /**
     * Walks up the hierarchy to the common ancestor. The exit action of the left state is executed. If the left state has history when the previously
     * active child must be added to the history.
     *
     * @param event    event triggering the state machine change
     * @param ancestor the common ancestor of the source and target states of the fired transition
     */
    private void exitStatesToCommonAncestor(Event<E> event, State<O, S, E> ancestor) {
        State<O, S, E> leaf = null;
        var state = activeStates.getLast();
        while (state != ancestor) {
            helper.executeExitAction(state, event);
            try {
                state.executeExitAction(owner, event);
            } catch (Exception e) {
                helper.throwException(state, state.exitAction(), event, e);
            }
            helper.exitState(state);
            activeStates.removeLast();
            if ((leaf != null) && state.hasHistory()) {
                history.add(leaf);
                history.add(state);
            }
            leaf = state;
            state = activeStates.getLast();
        }
    }

    /**
     * Walks down the node hierarchy from the common ancestor down to the target state of the fired transition.
     *
     * @param event     event triggering the state machine change
     * @param hierarchy list of states defining the containing hierarchy from the ancestor to the leaf node
     */
    private void enterStatesFromCommonAncestor(Event<E> event, Deque<State<O, S, E>> hierarchy) {
        hierarchy.removeFirst();
        for (var state : hierarchy) {
            helper.enterState(state);
            activeStates.addLast(state);
            helper.executeEntryAction(state, event);
            try {
                state.executeEntryAction(owner, event);
            } catch (Exception e) {
                helper.throwException(state, state.exitAction(), event, e);
            }
            history.remove(state);
        }
    }

    private void initiateStateWithHistory(@NotNull Event<E> event, @NotNull State<O, S, E> state) {
        var node = state;
        while ((node != null) && node.isComposite()) {
            if (node.hasHistory()) {
                Set<State<O, S, E>> historyStates = new HashSet<>(node.substates());
                historyStates.retainAll(history);
                if (historyStates.isEmpty()) {
                    node = node.initialState();
                } else {
                    node = historyStates.iterator().next();
                }
            } else {
                node = node.initialState();
            }
            if (node != null) {
                node.executeEntryAction(owner, event);
                activeStates.addLast(node);
                history.remove(node);
            }
        }
    }

    /**
     * Returns the common ancestor of the current active object from the root down to the common ancestor. The current hierarchy is stored in active
     * states field.
     *
     * @param state state which ancestor is searched in the list of active states
     * @return the common ancestor
     */
    private Deque<State<O, S, E>> getHierarchyUpToCommonAncestor(@NotNull State<O, S, E> state) {
        var hierarchy = root.getHierarchyFor(state);
        State<O, S, E> ancestor = null;
        while (!hierarchy.isEmpty() && activeStates.contains(hierarchy.getFirst())) {
            ancestor = hierarchy.pollFirst();
        }
        if (ancestor != null) {
            hierarchy.addFirst(ancestor);
        }
        return hierarchy;
    }
}
