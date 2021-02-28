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

package net.tangly.fsm.utilities;

import java.util.function.BiConsumer;

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.StateMachine;
import net.tangly.fsm.StateMachineEventHandler;
import net.tangly.fsm.Transition;

/**
 * The finite state machine dynamic checker verifies the correctness of a state machine description. The following rules are applied. <uL> <li>Only one
 * transition is eligible upon processing of an event otherwise the state machine behavior is undefined because the machine is not deterministic as expected in
 * hierarchical Harel state machines.</li> <li>No exception should be thrown during the processing of a condition or an action.</li>
 * </uL> <p> For each violation an illegal state exception will be thrown.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public record DynamicChecker<O, S extends Enum<S>, E extends Enum<E>>(StateMachine<O, S, E> fsm) implements StateMachineEventHandler<O, S, E> {
    @Override
    public void processEvent(Event<E> event) {
        // No checks defined
    }

    @Override
    public void wasReset() {
        // No checks defined
    }

    @Override
    public void fireLocalTransition(Transition<O, S, E> transition, Event<E> event) {
        if (transition.source().localTransitions().stream().filter(o -> o.evaluate(fsm.context(), event)).count() > 1) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void fireTransition(Transition<O, S, E> transition, Event<E> event) {
        if (transition.source().transitions().stream().filter(o -> o.evaluate(fsm.context(), event)).count() > 1) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void throwException(Transition<O, S, E> transition, Event<E> event, Exception e) {
        throw new IllegalStateException(e);
    }

    @Override
    public void throwException(State<O, S, E> state, BiConsumer<O, Event<E>> action, Event<E> event, Exception e) {
        throw new IllegalStateException(e);
    }
}
