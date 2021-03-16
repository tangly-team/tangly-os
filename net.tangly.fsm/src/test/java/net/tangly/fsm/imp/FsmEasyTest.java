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

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.dsl.FsmBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test implements the turnstile example of easy states library see <a href="https://github.com/j-easy/easy-states">easy-states</a>.
 */
public class FsmEasyTest {
    /**
     * States of the finite state machine.
     */
    private enum States {
        Root, Locked, Unlocked
    }

    /**
     * Events of the finite state machine washer.
     */
    private enum Events {
        Push, Coin
    }

    /**
     * Returns the builder for the finite state machine with all states, transitions, guards and actions. The construction of the finite state machine is quite
     * compacter with our DSL when compared with <i>easy-states</i>.
     *
     * @return builder instance for the finite state machine
     */
    private static FsmBuilder<FsmEasyTest, FsmEasyTest.States, FsmEasyTest.Events> build() {
        FsmBuilder<FsmEasyTest, FsmEasyTest.States, FsmEasyTest.Events> builder = FsmBuilder.of(FsmEasyTest.States.Root);
        builder.addToRoot(States.Locked, "Locked").isInitial();
        builder.addToRoot(States.Unlocked, "Unlocked");

        builder.in(States.Locked).on(Events.Push).to(States.Locked, "pushLocked").build();
        builder.in(States.Locked).on(Events.Coin).to(States.Unlocked, "unlock").build();
        builder.in(States.Unlocked).on(Events.Push).to(States.Locked, "lock").build();
        builder.in(States.Unlocked).on(Events.Coin).to(States.Unlocked, "coinUnlocked").build();
        return builder;
    }

    @Test
    void testTransitions() {
        State<FsmEasyTest, FsmEasyTest.States, FsmEasyTest.Events> root = build().definition();
        StateMachineImp<FsmEasyTest, FsmEasyTest.States, FsmEasyTest.Events> fsm = new StateMachineImp<>("easy-rules", root, this);
        assertThat(fsm.getActiveStates()).contains(root.getStateFor(States.Locked));
        fsm.fire(Event.of(Events.Push));
        assertThat(fsm.getActiveStates()).contains(root.getStateFor(States.Locked));
        assertThat(fsm.getActiveStates().size()).isSameAs(2);
        fsm.fire(Event.of(Events.Coin));
        assertThat(fsm.getActiveStates()).contains(root.getStateFor(States.Unlocked));
        assertThat(fsm.getActiveStates().size()).isSameAs(2);
        fsm.fire(Event.of(Events.Coin));
        assertThat(fsm.getActiveStates()).contains(root.getStateFor(States.Unlocked));
        assertThat(fsm.getActiveStates().size()).isSameAs(2);
        fsm.fire(Event.of(Events.Push));
        assertThat(fsm.getActiveStates()).contains(root.getStateFor(States.Locked));
        assertThat(fsm.getActiveStates().size()).isSameAs(2);
    }
}
