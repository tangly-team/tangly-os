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

package net.tangly.fsm.dsl;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.tangly.fsm.Event;

/**
 * Domain specific language fluent interface to configure a new transition.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */

public interface TransitionBuilder<O, S extends Enum<?>, E extends Enum<E>> {
    /**
     * Once a transition is selected, you can addToRoot an optional guard.
     *
     * @param guard guard to addToRoot to the transition
     * @return the transition builder to complete the configuration of a transition
     */
    default TransitionBuilder<O, S, E> onlyIf(BiPredicate<O, Event<E>> guard) {
        return onlyIf(guard, null);
    }

    /**
     * Once a transition is selected, you can addToRoot an optional guard.
     *
     * @param guard       guard to addToRoot to the transition
     * @param description description of the action
     * @return the transition builder to complete the configuration of a transition
     */
    TransitionBuilder<O, S, E> onlyIf(BiPredicate<O, Event<E>> guard, String description);

    /**
     * Once a transition is selected, you can addToRoot an optional guard.
     *
     * @param guardWithoutEvent guard to addToRoot to the transition
     * @return the transition builder to complete the configuration of a transition
     */
    default TransitionBuilder<O, S, E> onlyIf(Predicate<O> guardWithoutEvent) {
        return onlyIf((o, e) -> guardWithoutEvent.test(o));
    }

    /**
     * Once a transition is selected, you can add an optional guard.
     *
     * @param guardWithoutEvent guard to add to the transition
     * @param description       description of the action
     * @return the transition builder to complete the configuration of a transition
     */
    default TransitionBuilder<O, S, E> onlyIf(Predicate<O> guardWithoutEvent, String description) {
        return onlyIf((o, e) -> guardWithoutEvent.test(o), description);
    }

    /**
     * Once a transition is selected, you can addToRoot an optional guard.
     *
     * @param action action to addToRoot to the transition
     * @return the transition builder to complete the configuration of a transition
     */
    default TransitionBuilder<O, S, E> execute(BiConsumer<O, Event<E>> action) {
        return execute(action, null);
    }

    /**
     * Once a transition is selected, you can addToRoot an optional guard.
     *
     * @param action      action to addToRoot to the transition
     * @param description description of the action
     * @return the transition builder to complete the configuration of a transition
     */
    TransitionBuilder<O, S, E> execute(BiConsumer<O, Event<E>> action, String description);

    /**
     * Once a transition is selected, you can addToRoot an optional guard.
     *
     * @param actionWithoutEvent action to addToRoot to the transition
     * @return the transition builder to complete the configuration of a transition
     */
    default TransitionBuilder<O, S, E> execute(Consumer<O> actionWithoutEvent) {
        return execute((o, e) -> actionWithoutEvent.accept(o));
    }

    /**
     * Once a transition is selected, you can addToRoot an optional guard.
     *
     * @param actionWithoutEvent action to addToRoot to the transition
     * @param description        description of the action
     * @return the transition builder to complete the configuration of a transition
     */
    default TransitionBuilder<O, S, E> execute(Consumer<O> actionWithoutEvent, String description) {
        return execute((o, e) -> actionWithoutEvent.accept(o), description);
    }

    /**
     * Build the transition and add it to the context.
     * @return the state builder of the source transaction
     */
    StateBuilder<O,S,E> build();
}
