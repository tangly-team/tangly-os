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

package net.tangly.fsm.imp;

import net.tangly.fsm.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The implementation of the state machine abstraction.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
class StateMachineImp<O, S extends Enum<S>, E extends Enum<E>> implements StateMachine<O, S, E> {

    /**
     * Human-readable name of the state machine instance.
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
     * @param name  human-readable name of the finite state machine instance
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
        initialize();
    }

    @Override
    public void reset() {
        initialize();
        helper.wasReset();
    }

    @Override
    public boolean fire(@NotNull Event<E> event) {
        helper.processEvent(event);
        boolean fired = fireLocalTransition(event);
        if (!fired) {
            fired = fireTransition(event);
        }
        return fired;
    }

    @Override
    public State<O, S, E> root() {
        return root;
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
                        var hierarchy = getHierarchyToFirstActiveStaterFor(transition.target());
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
    public void addEventHandler(@NotNull StateMachineEventHandler<O, S, E> handler) {
        helper.addEventHandler(handler);
    }

    @Override
    public void removeEventHandler(@NotNull StateMachineEventHandler<O, S, E> handler) {
        helper.removeEventHandler(handler);
    }

    @Override
    public boolean isRegistered(@NotNull StateMachineEventHandler<O, S, E> handler) {
        return helper.isRegistered(handler);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "%s[".formatted(this.getClass().getSimpleName()), "]").add("name=%s".formatted(name))
            .add("activeStates=%s".formatted(activeStates)).add("history=%s".formatted(history)).toString();
    }

    /**
     * Return the collection of currently active states.
     *
     * @return the active states unmodifiable collection
     */
    Collection<State<O, S, E>> activeStates() {
        return Collections.unmodifiableCollection(activeStates);
    }

    /**
     * Return the collection of current history states.
     *
     * @return the history states as an unmodifiable collection
     */
    Collection<State<O, S, E>> historyStates() {
        return Collections.unmodifiableCollection(history);
    }

    /**
     * Initialize the final state machine to the hierarchical start state.
     */
    private void initialize() {
        activeStates.clear();
        history.clear();
        activeStates.add(root);
        enterStatesFromCommonAncestor(null, root.initialStates());
    }

    /**
     * Walk up the hierarchy to the common ancestor. The exit action of the left state is executed. If the left state has history when the previously active
     * child must be added to the history.
     *
     * @param event    event triggering the state machine change
     * @param ancestor the common ancestor of the source and target states of the fired transition
     */
    private void exitStatesToCommonAncestor(@NotNull Event<E> event, @NotNull State<O, S, E> ancestor) {
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
     * Walk down the node hierarchy from the common ancestor down to the target state of the fired transition.
     *
     * @param event     event triggering the state machine change
     * @param hierarchy list of states defining the containing hierarchy from the ancestor to the leaf node
     */
    private void enterStatesFromCommonAncestor(Event<E> event, @NotNull Deque<State<O, S, E>> hierarchy) {
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
     * Return the common ancestor between the current active objects and given state.
     *
     * @param state state which ancestor is searched in the list of active states
     * @return the common ancestor
     */
    private Deque<State<O, S, E>> getHierarchyToFirstActiveStaterFor(@NotNull State<O, S, E> state) {
        var ancestors = root.getHierarchyFor(state);
        State<O, S, E> ancestor = null;
        while (!ancestors.isEmpty() && activeStates.contains(ancestors.getFirst()) && !state.equals(ancestors.getFirst())) {
            ancestor = ancestors.removeFirst();
        }
        ancestors.addFirst(ancestor);
        return ancestors;
    }
}
