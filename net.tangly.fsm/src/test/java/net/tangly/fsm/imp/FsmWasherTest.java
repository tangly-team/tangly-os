/*
 *
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.fsm.imp;

import net.tangly.fsm.Event;
import net.tangly.fsm.State;
import net.tangly.fsm.dsl.FsmBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The test implements the final state machine modeling a washing machine.
 */
class FsmWasherTest {

    enum States {
        Root, Running, End, Washing, Rinsing, Drying, PowerOff
    }

    enum Events {
        Rinse, Dry, Stop, RestorePower, CutPower
    }

    static FsmBuilder<FsmWasherTest, States, Events> build() {
        FsmBuilder<FsmWasherTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.Running, "Running State").isInitial().hasHistory();
        builder.in(States.Running).add(States.Washing, "Wahsing State").isInitial();
        builder.in(States.Running).add(States.Rinsing, "Rising State");
        builder.in(States.Running).add(States.Drying, "Drying State");
        builder.addToRoot(States.PowerOff, "PowerOff State");
        builder.addToRoot(States.End, "State End");
        builder.in(States.Washing).on(Events.Rinse).to(States.Rinsing, "Wahsing -> Rinsing");
        builder.in(States.Rinsing).on(Events.Dry).to(States.Drying, "Rinsing -> Drying");
        builder.in(States.Running).on(Events.CutPower).to(States.PowerOff, "Running -> PowerOff");
        builder.in(States.PowerOff).on(Events.RestorePower).to(States.Running, "PowerOff -> Running");
        builder.in(States.Running).on(Events.Stop).to(States.End, "Running -> End");
        return builder;
    }

    @Test
    void testTransitions() {
        State<FsmWasherTest, States, Events> root = build().definition();
        StateMachineImp<FsmWasherTest, States, Events> fsm = new StateMachineImp<>("Washer", root, this);
        assertTrue(fsm.getActiveStates().contains(root.getStateFor(States.Running)));
        assertTrue(fsm.getActiveStates().contains(root.getStateFor(States.Washing)));
        assertTrue(fsm.getHistoryStates().isEmpty());
        fsm.fire(new Event<>(Events.Rinse));
        assertTrue(fsm.getActiveStates().contains(root.getStateFor(States.Rinsing)));
        fsm.fire(new Event<>(Events.CutPower));
        assertTrue(fsm.getActiveStates().contains(root.getStateFor(States.PowerOff)));
        assertTrue(fsm.getHistoryStates().contains(root.getStateFor(States.Rinsing)));
        assertTrue(fsm.getHistoryStates().contains(root.getStateFor(States.Running)));
        fsm.fire(new Event<>(Events.RestorePower));
        assertTrue(fsm.getActiveStates().contains(root.getStateFor(States.Running)));
        assertTrue(fsm.getActiveStates().contains(root.getStateFor(States.Rinsing)));
        assertTrue(fsm.getHistoryStates().isEmpty());
    }
}
