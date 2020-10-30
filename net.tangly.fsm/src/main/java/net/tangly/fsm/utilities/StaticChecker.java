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

package net.tangly.fsm.utilities;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import net.tangly.fsm.State;
import net.tangly.fsm.Transition;

/**
 * The finite state machine static checker verifies the correctness of a state machine description. The following rules are verified. <ul> <li>Each enumeration
 * value is used exactly once in the state machine description.</li> <li>A state has at most one initial substate.</li> <li>Each composite state with a
 * transition ending on it has an initial state.</li> <li>The state machine has a clear set of initial states so that it can be initialized properly.</li> <li>A
 * final state has no outgoing transition.</li> </ul>
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public class StaticChecker<O, S extends Enum<S>, E extends Enum<E>> implements Checker {
    private static final String BUNDLE_NAME = "net.tangly.fsm.utilities.CheckerMessages";

    private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Checks all static rules on the given final state machine description.
     *
     * @param root the final state machine description to check
     * @return the list of detected error messages
     */

    public List<String> check(State<O, S, E> root) {
        List<String> messages = new ArrayList<>();
        messages.addAll(checkStateIdUsedOnce(root));
        messages.addAll(checkStateHasAtMostOneInitialState(root));
        messages.addAll(checkDefinedInitialStates(root));
        messages.addAll(checkStateWithAfferentTransitionHasInitialState(root));
        return messages;
    }

    /**
     * Checks each enumeration value is used exactly once to identify a state of the machine.
     *
     * @param root the final state machine description to check
     * @return the list of detected error messages
     */

    public List<String> checkStateIdUsedOnce(State<O, S, E> root) {
        List<String> messages = new ArrayList<>();
        Set<State<O, S, E>> states = collectAllSubstates(root);
        EnumSet<S> allValues = EnumSet.allOf(root.id().getDeclaringClass());
        EnumSet<S> values = EnumSet.noneOf(root.id().getDeclaringClass());
        for (State<O, S, E> state : states) {
            if (values.contains(state.id())) {
                messages.add(createError(bundle, "FSM-STAT-001", state.id()));
            } else {
                values.add(state.id());
            }
        }
        messages.addAll(allValues.stream().filter(state -> !values.contains(state)).map(state -> createError(bundle, "FSM-STAT-002", state))
                .collect(Collectors.toList()));
        return messages;
    }

    /**
     * Checks each composite state has at most one substate with the initial flag.
     *
     * @param root the final state machine description to check
     * @return the list of detected error messages
     */
    public List<String> checkStateHasAtMostOneInitialState(State<O, S, E> root) {
        return collectAllSubstates(root).stream().filter(State::isComposite).filter(state -> getInitialSubstates(state).size() > 1)
                .map(state -> createError(bundle, "FSM-STAT-003", state.id(), getInitialSubstates(state).size())).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Checks that the finite state machine can be initialized at startup by selecting one unique initial state.
     *
     * @param root the final state machine description to check
     * @return the list of detected error messages
     */
    List<String> checkDefinedInitialStates(State<O, S, E> root) {
        List<String> messages = new ArrayList<>();
        State<O, S, E> state = root;
        while ((state != null) && !state.substates().isEmpty()) {
            Set<State<O, S, E>> initialStates = getInitialSubstates(state);
            if (initialStates.size() != 1) {
                messages.add(createError(bundle, "FSM-STAT-005", state.id(), initialStates.size()));
                state = null;
            } else {
                state = initialStates.iterator().next();
            }
        }
        return messages;
    }

    /**
     * Checks each composite state with an afferent transition has an initial state.
     *
     * @param state the final state machine description to check
     * @return the list of detected error messages
     */

    public List<String> checkStateWithAfferentTransitionHasInitialState(State<O, S, E> state) {
        return collectAllSubstates(state).stream().flatMap(o -> o.transitions().stream()).map(Transition::target).distinct().filter(State::isComposite)
                .filter(o -> getInitialSubstates(o).size() != 1).map(o -> createError(bundle, "FSM-STAT-004", o.id(), getInitialSubstates(o).size()))
                .collect(Collectors.toList());
    }


    private Set<State<O, S, E>> collectAllSubstates(State<O, S, E> root) {
        return collectAllSubstates(new HashSet<>(), root);
    }

    private Set<State<O, S, E>> collectAllSubstates(Set<State<O, S, E>> states, State<O, S, E> root) {
        states.add(root);
        root.substates().forEach(o -> collectAllSubstates(states, o));
        return states;
    }

    private Set<State<O, S, E>> getInitialSubstates(State<O, S, E> state) {
        Set<State<O, S, E>> initialStates = new HashSet<>();
        state.substates().stream().filter(State::isInitial).forEach(initialStates::add);
        return initialStates;
    }
}
