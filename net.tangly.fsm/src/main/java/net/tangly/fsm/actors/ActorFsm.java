/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.fsm.actors;

import net.tangly.fsm.Event;
import net.tangly.fsm.StateMachine;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * A local actor implements the actor contract as a local thread running in the virtual machine. An actor completes when its finite state machine reaches a final state and the
 * associated thread is returned to the pool of available threads.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public class ActorFsm<O extends ActorFsm<O, S, E>, S extends Enum<S>, E extends Enum<E>> extends ActorImp<Event<E>> {
    /**
     * The final state machine defining the behavior of the actor class.
     */
    private final StateMachine<O, S, E> fsm;

    /**
     * Constructor of the class.
     *
     * @param builder builder to create the finite state machine of the local actor
     * @param name    name of the local actor
     */
    @SuppressWarnings("unchecked")
    public ActorFsm(@NotNull FsmBuilder<O, S, E> builder, @NotNull String name) {
        super(name);
        this.fsm = builder.machine(name, (O) this);
    }

    @Override
    protected boolean process(@NotNull Event<E> msg) {
        return fsm.fire(msg);
    }
}
