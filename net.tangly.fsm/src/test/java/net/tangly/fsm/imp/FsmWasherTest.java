/*
 * Copyright 2006-2022 Marcel Baumann
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

import net.tangly.fsm.Event;
import net.tangly.fsm.dsl.FsmBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test implements the final state machine modeling a washing machine. The code is also used as snippets for the documentation.
 */
class FsmWasherTest {

    /**
     * States of the finite state machine washer.
     */
    enum States {
        Root, Running, End, Washing, Rinsing, Drying, PowerOff
    }

    /**
     * Events of the finite state machine washer.
     */
    enum Events {
        Rinse, Dry, Stop, RestorePower, CutPower
    }

    /**
     * Returns the builder for the finite state machine with all states, transitions, guards and actions.
     *
     * @return builder instance for the finite state machine
     */
    static FsmBuilder<FsmWasherTest, States, Events> build() {
        // @start region="declare-fsm-washer-with-builder"

        // add states to the FSM declaration. States can be an initial state, a final state, or have history.
        FsmBuilder<FsmWasherTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.Running, "Running State").isInitial().hasHistory();
        builder.in(States.Running).add(States.Washing, "Washing State").isInitial();
        builder.in(States.Running).add(States.Rinsing, "Rising State");
        builder.in(States.Running).add(States.Drying, "Drying State");
        builder.addToRoot(States.PowerOff, "PowerOff State");
        builder.addToRoot(States.End, "State End");

        // add transitions, actions, and events triggering the transition to the FSM declaration
        builder.in(States.Washing).on(Events.Rinse).to(States.Rinsing, "Washing -> Rinsing").build();
        builder.in(States.Rinsing).on(Events.Dry).to(States.Drying, "Rinsing -> Drying").build();
        builder.in(States.Running).on(Events.CutPower).to(States.PowerOff, "Running -> PowerOff").build();
        builder.in(States.PowerOff).on(Events.RestorePower).to(States.Running, "PowerOff -> Running").build();
        builder.in(States.Running).on(Events.Stop).to(States.End, "Running -> End").build();

        // return the builder
        return builder;
        // @end
    }

    @Test
    void testTransitions() {
        StateMachineImp<FsmWasherTest, States, Events> fsm = new StateMachineImp<>("Washer", build().definition(), this);
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.Running));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.Washing));
        assertThat(fsm.historyStates()).isEmpty();
        fsm.fire(Event.of(Events.Rinse));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.Rinsing));
        fsm.fire(Event.of(Events.CutPower));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.PowerOff));
        assertThat(fsm.historyStates()).contains(fsm.root().getStateFor(States.Rinsing));
        assertThat(fsm.historyStates()).contains(fsm.root().getStateFor(States.Running));
        fsm.fire(Event.of(Events.RestorePower));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.Running));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.Rinsing));
        assertThat(fsm.historyStates()).isEmpty();
    }
}
