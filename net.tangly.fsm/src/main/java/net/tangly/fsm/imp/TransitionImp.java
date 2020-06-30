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

package net.tangly.fsm.imp;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.Transition;

/**
 * Default implementation of an mutable class providing the transition immutable interface.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
class TransitionImp<O, S extends Enum<S>, E extends Enum<E>> implements Transition<O, S, E> {
    /**
     * The source or origin state of the transition.
     */
    private final State<O, S, E> source;

    /**
     * The target or destination state of the transition.
     */
    private final State<O, S, E> target;

    /**
     * The event identifier which triggers the transition.
     */
    private final E eventId;

    /**
     * The optional guard which allows conditional trigger of the transition.
     */
    private BiPredicate<O, Event<E>> guard;

    /**
     * The optional action to accept when the transition is fired.
     */
    private BiConsumer<O, Event<E>> action;

    private String description;
    private String guardDescription;
    private String actionDescription;

    /**
     * Constructor of the class.
     *
     * @param source  source or start node of the transition
     * @param target  target or end node of the transition
     * @param eventId identifier of the event triggering the transition
     */
    TransitionImp(State<O, S, E> source, State<O, S, E> target, E eventId) {
        this(source, target, eventId, null, null);
    }

    /**
     * Constructor of the class.
     *
     * @param source  source or start node of the transition
     * @param target  target or end node of the transition
     * @param eventId identifier of the event triggering the transition
     * @param action  action executed when the transition is fired
     */
    TransitionImp(State<O, S, E> source, State<O, S, E> target, E eventId, BiConsumer<O, Event<E>> action) {
        this(source, target, eventId, null, action);
    }

    /**
     * Constructor of the class.
     *
     * @param source  source or start node of the transition
     * @param target  target or end node of the transition
     * @param eventId identifier of the event triggering the transition
     * @param guard   guard controlling the firing of the transition
     * @param action  action executed when the transition is fired
     */
    TransitionImp(State<O, S, E> source, State<O, S, E> target, E eventId, BiPredicate<O, Event<E>> guard, BiConsumer<O, Event<E>> action) {
        this.source = source;
        this.target = target;
        this.eventId = eventId;
        this.guard = guard;
        this.action = action;
    }

    @Override
    public State<O, S, E> target() {
        return target;
    }

    @Override
    public State<O, S, E> source() {
        return source;
    }

    @Override
    public E eventId() {
        return eventId;
    }

    @Override
    public BiPredicate<O, Event<E>> guard() {
        return guard;
    }

    /**
     * Sets the guard of the transition.
     *
     * @param guard guard of the transition
     * @see #guard()
     */
    public void guard(BiPredicate<O, Event<E>> guard) {
        this.guard = guard;
    }

    @Override
    public BiConsumer<O, Event<E>> action() {
        return action;
    }

    /**
     * Sets the action of the transition.
     *
     * @param action action executed when the transition is fired
     * @see #action()
     */
    public void action(BiConsumer<O, Event<E>> action) {
        this.action = action;
    }

    @Override
    public String actionDescription() {
        return actionDescription;
    }

    /**
     * Sets the description of the action.
     *
     * @param description new description of the action
     * @see #actionDescription()
     */
    public void setActionDescription(String description) {
        this.actionDescription = description;
    }

    @Override
    public String description() {
        return description;
    }

    /**
     * Sets the description of the transition.
     *
     * @param description new description of the action
     * @see #description()
     */
    public void description(String description) {
        this.description = description;
    }

    @Override
    public String guardDescription() {
        return guardDescription;
    }

    /**
     * Sets the description of the guard.
     *
     * @param description new description of the guard
     * @see #guardDescription()
     */
    public void guardDescription(String description) {
        this.guardDescription = description;
    }


    @Override
    public boolean evaluate(O context, Event<E> event) {
        return (event.type() == this.eventId()) && (!hasGuard() || guard.test(context, event));
    }

    @Override
    public boolean execute(O context, Event<E> event) {
        boolean canBeFired = evaluate(context, event);
        if (canBeFired && hasAction()) {
            action.accept(context, event);
        }
        return canBeFired;
    }

    @Override
    public String toString() {
        return String.format("Transition(%s -> %s : %s [%s] %s : %s)", source.id(), target.id(), eventId, guardDescription, actionDescription,
                description);
    }
}
