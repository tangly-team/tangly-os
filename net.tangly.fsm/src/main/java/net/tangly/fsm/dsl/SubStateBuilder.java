/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.fsm.dsl;

import net.tangly.fsm.Event;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Domain specific language fluent interface to configure a new state.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */

public interface SubStateBuilder<O, S extends Enum<?>, E extends Enum<E>> {

    /**
     * Sets the history flag of the state to true.
     *
     * @return the builder to continue defining the state
     */
    default SubStateBuilder<O, S, E> hasHistory() {
        return hasHistory(true);
    }

    /**
     * Sets the history flag of the state.
     *
     * @param hasHistory new flag value to set
     * @return the builder to continue defining the state
     */
    SubStateBuilder<O, S, E> hasHistory(boolean hasHistory);

    /**
     * Sets the initial flag of the state to true.
     *
     * @return the builder to continue defining the state
     */
    SubStateBuilder<O, S, E> isInitial();

    /**
     * Sets the initial flag of the state.
     *
     * @param initial new flag value to set
     * @return the builder to continue defining the state
     */
    SubStateBuilder<O, S, E> isInitial(boolean initial);

    /**
     * Sets the entry action of the state.
     *
     * @param action entry action of the state
     * @return the builder to continue defining the state
     */
    default SubStateBuilder<O, S, E> onEntry(BiConsumer<O, Event<E>> action) {
        return onEntry(action, null);
    }

    /**
     * Sets the entry action of the state.
     *
     * @param action      entry action of the state
     * @param description description of the action
     * @return the builder to continue defining the state
     */
    SubStateBuilder<O, S, E> onEntry(BiConsumer<O, Event<E>> action, String description);

    /**
     * Sets the entry action of the state.
     *
     * @param actionWithoutEvent entry action of the state
     * @return the builder to continue defining the state
     */
    default SubStateBuilder<O, S, E> onEntry(Consumer<O> actionWithoutEvent) {
        return onEntry((o, e) -> actionWithoutEvent.accept(o));
    }

    /**
     * Sets the entry action of the state.
     *
     * @param actionWithoutEvent entry action of the state
     * @param description        description of the action
     * @return the builder to continue defining the state
     */
    default SubStateBuilder<O, S, E> onEntry(Consumer<O> actionWithoutEvent, String description) {
        return onEntry((o, e) -> actionWithoutEvent.accept(o), description);
    }

    /**
     * Sets the exit action of the state.
     *
     * @param action exit action of the state
     * @return the builder to continue defining the state
     */
    default SubStateBuilder<O, S, E> onExit(BiConsumer<O, Event<E>> action) {
        return onExit(action, null);
    }

    /**
     * Sets the exit action of the state.
     *
     * @param action      exit action of the state
     * @param description description of the action
     * @return the builder to continue defining the state
     */
    SubStateBuilder<O, S, E> onExit(BiConsumer<O, Event<E>> action, String description);

    /**
     * Sets the exit action of the state.
     *
     * @param actionWithoutEvent exit action of the state
     * @return the builder to continue defining the state
     */
    default SubStateBuilder<O, S, E> onExit(Consumer<O> actionWithoutEvent) {
        return onExit((o, e) -> actionWithoutEvent.accept(o));
    }

    /**
     * Sets the exit action of the state.
     *
     * @param actionWithoutEvent exit action of the state
     * @param description        description of the action
     * @return the builder to continue defining the state
     */
    default SubStateBuilder<O, S, E> onExit(Consumer<O> actionWithoutEvent, String description) {
        return onExit((o, e) -> actionWithoutEvent.accept(o), description);
    }
}
