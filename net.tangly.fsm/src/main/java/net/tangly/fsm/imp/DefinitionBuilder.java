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

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.StateMachine;
import net.tangly.fsm.Transition;
import net.tangly.fsm.dsl.*;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * The class implements the interfaces to build a complete finite state machine definition using a fluent DSL - Domain Specific Language -. Care was taken to
 * balance expression power, compact syntax, and syntax sugar.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public class DefinitionBuilder<O, S extends Enum<S>, E extends Enum<E>>
    implements FsmBuilder<O, S, E>, StateBuilder<O, S, E>, SubStateBuilder<O, S, E>, ToTransitionBuilder<O, S, E>, TransitionBuilder<O, S, E> {
    private final EnumMap<S, StateImp<O, S, E>> states;
    private final StateImp<O, S, E> root;
    private StateImp<O, S, E> context;
    private StateImp<O, S, E> target;
    private E eventId;
    private boolean isLocalTransition;
    private BiPredicate<O, Event<E>> guard;
    private BiConsumer<O, Event<E>> action;
    private String description;
    private String actionDescription;
    private String guardDescription;

    /**
     * Constructor of the class. Creates a finite state machine builder with the given root state identifier.
     *
     * @param rootId identifier of the root state for the state machine definition under construction
     */
    public DefinitionBuilder(@NotNull S rootId) {
        root = new StateImp<>(rootId);
        root.setInitial(true);
        states = new EnumMap<>((Class<S>) rootId.getClass());
    }

    // region StateBuilder

    @Override
    public State<O, S, E> definition() {
        return root;
    }

    @Override
    public StateMachine<O, S, E> machine(String name, O owner) {
        return new StateMachineImp<>(name, definition(), owner);
    }

    @Override
    public StateBuilder<O, S, E> in(@NotNull S stateId) {
        if (!states.containsKey(stateId)) {
            throw new IllegalArgumentException();
        }
        context = states.get(stateId);
        return this;
    }

    @Override
    public StateBuilder<O, S, E> root() {
        context = root;
        return this;
    }

    @Override
    public SubStateBuilder<O, S, E> add(@NotNull S stateId, String description) {
        if (states.containsKey(stateId)) {
            throw new IllegalArgumentException();
        }
        StateImp<O, S, E> state = new StateImp<>(stateId);
        state.setDescription(description);
        states.put(stateId, state);
        context.addSubstate(state);
        context = state;
        return this;
    }

    // endregion

    // region TransitionBuilder

    @Override
    public TransitionBuilder<O, S, E> to(@NotNull S stateId, String description) {
        if (!states.containsKey(stateId) || (eventId == null)) {
            throw new IllegalArgumentException();
        }
        this.isLocalTransition = false;
        this.target = states.get(stateId);
        this.description = description;
        return this;
    }

    @Override
    public TransitionBuilder<O, S, E> onLocal(@NotNull E eventId, String description) {
        this.isLocalTransition = true;
        this.target = context;
        this.eventId = eventId;
        this.description = description;
        return this;
    }

    @Override
    public TransitionBuilder<O, S, E> onlyIf(@NotNull BiPredicate<O, Event<E>> guard, String guardDescription) {
        this.guard = guard;
        this.guardDescription = guardDescription;
        return this;
    }

    @Override
    public TransitionBuilder<O, S, E> execute(@NotNull BiConsumer<O, Event<E>> action, String actionDescription) {
        this.action = action;
        this.actionDescription = actionDescription;
        return this;
    }

    @Override
    public StateBuilder<O, S, E> build() {
        Transition<O, S, E> transition = new TransitionImp<>(context, target, eventId, guard, action, description, guardDescription, actionDescription);
        if (isLocalTransition) {
            context.addLocalTransition(transition);
        } else {
            context.addTransition(transition);
        }
        resetTransactionContext();
        return this;
    }

    // endregion

    // region ToTransition builder

    @Override
    public ToTransitionBuilder<O, S, E> on(E eventId) {
        this.eventId = eventId;
        return this;
    }

    // endregion

    // region SubStateBuilder

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

    // endregion

    /**
     * Resets the builder context to clean.
     */
    private void resetTransactionContext() {
        isLocalTransition = false;
        target = null;
        eventId = null;
        guard = null;
        action = null;
        description = null;
        guardDescription = null;
        actionDescription = null;
    }
}
