/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.fsm.imp;

import java.util.Objects;
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
record TransitionImp<O, S extends Enum<S>, E extends Enum<E>>(State<O, S, E> source, State<O, S, E> target, E eventId, BiPredicate<O, Event<E>> guard,
                                                              BiConsumer<O, Event<E>> action, String description, String guardDescription,
                                                              String actionDescription) implements Transition<O, S, E> {
    public TransitionImp {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(eventId);
    }

    @Override
    public boolean evaluate(O context, Event<E> event) {
        return (event.type().equals(this.eventId()) && (!hasGuard() || guard.test(context, event)));
    }

    @Override
    public boolean execute(O context, Event<E> event) {
        boolean canBeFired = evaluate(context, event);
        if (canBeFired && hasAction()) {
            action.accept(context, event);
        }
        return canBeFired;
    }
}
