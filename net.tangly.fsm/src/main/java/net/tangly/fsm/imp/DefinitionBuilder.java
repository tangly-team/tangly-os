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
import net.tangly.fsm.dsl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public class DefinitionBuilder<O, S extends Enum<S>, E extends Enum<E>> implements FsmBuilder<O, S, E>, StateBuilder<O, S, E>, SubStateBuilder<O, S, E>, ToTransitionBuilder<O, S, E>, TransitionBuilder<O, S, E> {

    private final Map<S, StateImp<O, S, E>> states;
    private final StateImp<O, S, E> root;
    private StateImp<O, S, E> context;
    private E eventId;
    private TransitionImp<O, S, E> transition;

    /**
     * Constructor of the class.
     *
     * @param rootId identifier of the root state of the state machine definition under construction
     */
    public DefinitionBuilder(S rootId) {
        root = new StateImp<>(rootId);
        root.setInitial(true);
        states = new HashMap<>();
    }

    @Override
    public State<O, S, E> definition() {
        resetContext();
        return root;
    }

    @Override
    public StateMachine<O, S, E> machine(String name, O owner) {
        return new StateMachineImp<>(name, definition(), owner);
    }

    @Override
    public StateBuilder<O, S, E> in(S stateId) {
        checkArgument(states.containsKey(stateId));
        resetContext();
        context = states.get(stateId);
        return this;
    }

    @Override
    public StateBuilder<O, S, E> root() {
        resetContext();
        context = root;
        return this;
    }

    @Override
    public SubStateBuilder<O, S, E> add(S stateId, String description) {
        checkArgument(!states.containsKey(stateId));
        StateImp<O, S, E> state = new StateImp<>(stateId);
        state.setDescription(description);
        states.put(stateId, state);
        context.addSubstate(state);
        context = state;
        return this;
    }

    // region ToTransitionBuilder
    @Override
    public TransitionBuilder<O, S, E> to(S stateId, String description) {
        checkArgument(states.containsKey(stateId) && (eventId != null));
        transition = new TransitionImp<>(context, states.get(stateId), eventId);
        transition.setDescription(description);
        context.addTransition(transition);
        return this;
    }

    // endregion


    @Override
    public ToTransitionBuilder<O, S, E> on(E eventId) {
        this.eventId = eventId;
        return this;
    }

    @Override
    public TransitionBuilder<O, S, E> onLocal(E eventId, String description) {
        this.eventId = eventId;
        transition = new TransitionImp<>(context, context, eventId);
        transition.setDescription(description);
        context.addLocalTransition(transition);
        return this;
    }

    // region TransitionBuilder

    @Override
    public TransitionBuilder<O, S, E> onlyIf(BiPredicate<O, Event<E>> guard, String description) {
        transition.setGuard(guard);
        transition.setGuardDescription(description);
        return this;
    }

    @Override
    public TransitionBuilder<O, S, E> execute(BiConsumer<O, Event<E>> action, String description) {
        transition.setAction(action);
        transition.setActionDescription(description);
        return this;
    }

    // endregion

    @Override
    public SubStateBuilder<O, S, E> hasHistory(boolean hasHistory) {
        context.setHasHistory(hasHistory);
        return this;
    }

    @Override
    public SubStateBuilder<O, S, E> isInitial() {
        return isInitial(true);
    }

    @Override
    public SubStateBuilder<O, S, E> isInitial(boolean initial) {
        context.setInitial(initial);
        return this;
    }

    @Override
    public SubStateBuilder<O, S, E> onEntry(BiConsumer<O, Event<E>> action, String description) {
        context.setEntryAction(action);
        context.setEntryActionDescription(description);
        return this;
    }

    @Override
    public SubStateBuilder<O, S, E> onExit(BiConsumer<O, Event<E>> action, String description) {
        context.setExitAction(action);
        context.setExitActionDescription(description);
        return this;
    }

    /**
     * Resets the builder context to clean.
     */
    private void resetContext() {
        context = null;
        eventId = null;
        transition = null;
    }
}
