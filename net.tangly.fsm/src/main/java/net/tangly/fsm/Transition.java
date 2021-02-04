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

package net.tangly.fsm;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * The transition defines a link between two finite machine states. The class provides an immutable interface on the instances.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public interface Transition<O, S extends Enum<S>, E extends Enum<E>> {
    /**
     * Returns the start state or source state of the transition.
     *
     * @return source state of the transition
     */
    State<O, S, E> source();

    /**
     * Returns the end state or target state of the transition.
     *
     * @return end state of the transition
     */
    State<O, S, E> target();

    /**
     * Returns the identifier of the event triggering the firing of the transition.
     *
     * @return the identifier of the event
     */
    E eventId();

    /**
     * Returns the optional guard restricting the firing of the transition.
     *
     * @return guard of the transition if defined otherwise null
     */
    BiPredicate<O, Event<E>> guard();

    /**
     * Indicates if the transition has a guard or not.
     *
     * @return flag indicating if the transition has a guard
     * @see #guard()
     */
    default boolean hasGuard() {
        return guard() != null;
    }

    /**
     * Returns the optional action executed upon firing of the transition.
     *
     * @return action of the transition if defined otherwise null
     */

    BiConsumer<O, Event<E>> action();

    /**
     * Indicates if the transition has an action or not.
     *
     * @return flag indicating if the transition has an action
     * @see #action()
     */
    default boolean hasAction() {
        return action() != null;
    }

    /**
     * Evaluates the event and guard of the transition.
     *
     * @param context instance owning the finite state machine
     * @param event   event triggering the transition
     * @return true if the transition can be fired otherwise false
     */
    boolean evaluate(O context, Event<E> event);

    /**
     * Executes the action associated with the transition if defined. The action is only executed if the event and guard evaluates to true {@link
     * #evaluate(Object, Event)}.
     *
     * @param context instance owning the finite state machine
     * @param event   the event triggering the execution of the action
     * @return true if the transition was fired otherwise false
     */
    boolean execute(O context, Event<E> event);

    /**
     * Returns the human readable description of the transition.
     *
     * @return the description of the transition if defined otherwise null
     */
    String description();

    /**
     * Returns the human readable description of the guard of the transition.
     *
     * @return the description of the guard of the transition if defined otherwise null
     */
    String guardDescription();

    /**
     * Returns the human readable description of the action of the transition.
     *
     * @return the description of the action of the transition if defined otherwise null
     */
    String actionDescription();
}
