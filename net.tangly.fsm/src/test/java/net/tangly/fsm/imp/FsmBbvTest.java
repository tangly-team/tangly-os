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

package net.tangly.fsm.imp;

import net.tangly.fsm.Event;
import net.tangly.fsm.StateMachine;
import net.tangly.fsm.StateMachineEventHandler;
import net.tangly.fsm.dsl.FsmBuilder;
import net.tangly.fsm.utilities.StaticChecker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the example of bbv fsm library described https://code.google.com/p/bbvfsm/wiki/GettingStarted.
 */
class FsmBbvTest {
    @Test
    void simplyTurnOnAndOffTest() {
        var fsm = createFsm();
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB.exitDAB.exitOn.OnToOff.entryOff");
    }

    @Test
    void turnOnToggleModeTurnOffTurnOnTest() {
        var fsm = createFsm();
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
        fsm.fire(Event.of(FsmBbv.Events.ToggleMode));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitDAB.DABtoFM.entryFM.entryPlay");
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitPlay.exitFM.exitOn.OnToOff.entryOff");
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitOff.OffToOn.entryOn.entryFM.entryPlay");
    }

    @Test
    void turnOnToggleModeStationLostTurnOffTurnOnTest() {
        var fsm = createFsm();
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
        fsm.fire(Event.of(FsmBbv.Events.ToggleMode));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitDAB.DABtoFM.entryFM.entryPlay");
        fsm.fire(Event.of(FsmBbv.Events.StationLost));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitPlay.PlayToAutoTune.entryAutoTune");
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitAutoTune.exitFM.exitOn.OnToOff.entryOff");
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitOff.OffToOn.entryOn.entryFM.entryAutoTune");
    }

    @Test
    void whenMaintenanceTurnOnTurnOffTest() {
        var fsm = createFsm();
        fsm.context().setMaintenance(true);
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToMaintenance.entryMaintenance");
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitMaintenance.MaintenanceToOff.entryOff");
    }

    @Test
    void whenDabStoreStationTest() {
        var fsm = createFsm();
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        fsm.fire(Event.of(FsmBbv.Events.StoreStation));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB.DABToDAB");
    }

    @Test
    void whenPlayStoreStationTest() {
        var fsm = createFsm();
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
        fsm.fire(Event.of(FsmBbv.Events.ToggleMode));
        fsm.fire(Event.of(FsmBbv.Events.StoreStation));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitDAB.DABtoFM.entryFM.entryPlay.PlayToPlay");
    }

    @Test
    void whenReset() {
        var fsm = createFsm();
        fsm.reset();
        assertThat((fsm.isAlive())).isTrue();
    }

    @Test
    void whenHistory() {
        StateMachineImp<FsmBbv, FsmBbv.States, FsmBbv.Events> fsm = (StateMachineImp<FsmBbv, FsmBbv.States, FsmBbv.Events>) createFsm();
        fsm.fire(Event.of(FsmBbv.Events.TogglePower));
        fsm.fire(Event.of(FsmBbv.Events.ToggleMode));
        fsm.fire(Event.of(FsmBbv.Events.StationLost));

        // Root/On/FM/AutoTune,   []
        assertThat(fsm.historyStates()).isEmpty();
        assertThat(fsm.activeStates()).hasSize(4);
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.Root));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.On));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.FM));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.AutoTune));

        fsm.fire(Event.of(FsmBbv.Events.TogglePower));

        // Root/Off,   [On/FM/AutoTune]
        assertThat(fsm.activeStates()).hasSize(2);
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.Root));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.Off));
        assertThat(fsm.historyStates()).hasSize(3);
        assertThat(fsm.historyStates()).contains(fsm.root().getStateFor(FsmBbv.States.On));
        assertThat(fsm.historyStates()).contains(fsm.root().getStateFor(FsmBbv.States.FM));
        assertThat(fsm.historyStates()).contains(fsm.root().getStateFor(FsmBbv.States.AutoTune));


        fsm.fire(Event.of(FsmBbv.Events.TogglePower));

        // Root/On/FM/Autotune, []
        assertThat(fsm.historyStates()).isEmpty();
        assertThat(fsm.activeStates()).hasSize(4);
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.Root));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.On));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.FM));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmBbv.States.AutoTune));
    }

    @Test
    void independentParallelUsageTest() {
        FsmBuilder<FsmBbv, FsmBbv.States, FsmBbv.Events> builder = FsmBbv.build();
        var fsm1 = builder.machine("test-fsm1", new FsmBbv());
        var fsm2 = builder.machine("test-fsm2", new FsmBbv());
        fsm1.context().setMaintenance(true);
        fsm1.fire(Event.of(FsmBbv.Events.TogglePower));
        fsm2.fire(Event.of(FsmBbv.Events.TogglePower));
        assertThat(fsm1.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToMaintenance.entryMaintenance");
        assertThat(fsm2.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
    }

    @Test
    void staticCheckerTest() {
        FsmBuilder<FsmBbv, FsmBbv.States, FsmBbv.Events> builder = FsmBbv.build();
        StaticChecker<FsmBbv, FsmBbv.States, FsmBbv.Events> checker = new StaticChecker<>();
        assertThat(checker.check(builder.definition())).isEmpty();
        assertThat(checker.checkStateHasAtMostOneInitialState(builder.definition())).isEmpty();
        assertThat(checker.checkStateIdUsedOnce(builder.definition())).isEmpty();
        assertThat(checker.checkStateWithAfferentTransitionHasInitialState(builder.definition())).isEmpty();
    }

    private StateMachine<FsmBbv, FsmBbv.States, FsmBbv.Events> createFsm() {
        StateMachine<FsmBbv, FsmBbv.States, FsmBbv.Events> fsm = FsmBbv.build().machine("test-fsm", new FsmBbv());
        fsm.addEventHandler(new StateMachineEventHandler<>() {
        });
        return fsm;
    }
}
