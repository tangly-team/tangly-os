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

import net.tangly.fsm.dsl.FsmBuilder;

/**
 * The class defines the finite state machine of the <emp>bbv fsm</emp> open source project example.
 */
class FsmBbv {
    /**
     * The finite state machine internal states for the test configuration.
     */
    public enum States {
        Root, Off, Maintenance, On, FM, DAB, Play, AutoTune
    }

    /**
     * The finite state machine internal events for the test configuration.
     */
    public enum Events {
        TogglePower, ToggleMode, StationLost, StationFound, StoreStation
    }

    private StringBuilder log = new StringBuilder();
    private boolean maintenance;

    /**
     * Returns the builder for the finite state machine with all states, transitions, guards and actions.
     *
     * @return builder instance for the finite state machine
     */
    static FsmBuilder<FsmBbv, States, Events> build() {
        FsmBuilder<FsmBbv, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.Off).isInitial().onEntry(FsmBbv::logOffEntry, "Entry action of Off state").onExit(FsmBbv::logOffExit, "ExitOffstate");
        builder.addToRoot(States.Maintenance).onEntry(FsmBbv::logMaintenanceEntry).onExit(FsmBbv::logMaintenanceExit);
        builder.addToRoot(States.On).hasHistory().onEntry(FsmBbv::logOnEntry).onExit(FsmBbv::logOnExit);
        builder.in(States.On).add(States.DAB).isInitial().onEntry(FsmBbv::logDabEntry).onExit(FsmBbv::logDabExit);
        builder.in(States.DAB).onLocal(Events.StoreStation, "DAB -> DAB").execute((o, e) -> o.appendToLog("DABToDAB"));
        builder.in(States.On).add(States.FM).hasHistory().onEntry(FsmBbv::logFmEntry, "entryFM").onExit(FsmBbv::logFmExit, "exitFM");
        builder.in(States.FM).add(States.Play).isInitial().hasHistory().onEntry(FsmBbv::logPlayEntry, "entryPlay").onExit(FsmBbv::logPlayExit, "exitPlay");
        builder.in(States.Play).onLocal(Events.StoreStation, "Play -> Play").execute((o, e) -> o.appendToLog("PlayToPlay"));
        builder.in(States.FM).add(States.AutoTune).onEntry(FsmBbv::logAutoTuneEntry).onExit(FsmBbv::logAutoTuneExit);
        builder.in(States.Off).on(Events.TogglePower).to(States.Maintenance, "Off -> TogglePower").onlyIf(FsmBbv::isMaintenanceMode, "Maintenance is On")
                .execute(FsmBbv::logTransitionFromOffToMaintenance, "log transition Off to Maintenance");
        builder.in(States.Maintenance).on(Events.TogglePower).to(States.Off, "Maintenance -> Off")
                .execute(FsmBbv::logTransitionFromMaintenanceToOff, "MaintainedtoOff");
        builder.in(States.Off).on(Events.TogglePower).to(States.On, "Off -> On").onlyIf((o) -> !o.isMaintenanceMode(), "Maintenance Off")
                .execute(FsmBbv::logTransitionFromOffToOn, "OfftoOn");
        builder.in(States.On).on(Events.TogglePower).to(States.Off, "TogglePower -> On").execute(FsmBbv::logTransitionFromOnToOff, "OntoOff");
        builder.in(States.DAB).on(Events.ToggleMode).to(States.FM, "DAB -> FM").execute(FsmBbv::logTransitionFromDabToFm, "DABtoFM");
        builder.in(States.FM).on(Events.ToggleMode).to(States.DAB, "FM -> DAB").execute(FsmBbv::logTransitionFromFmToDab, "FMtoDAB");
        builder.in(States.Play).on(Events.StationLost).to(States.AutoTune, "Play -> AutoTune")
                .execute(FsmBbv::logTransitionFromPlayToAutoTune, "PlaytoAutoTune");
        builder.in(States.AutoTune).on(Events.StationFound).to(States.Play, "AutoTune -> Play")
                .execute(FsmBbv::logTransitionFromAutoTuneToPlay, "AutoTunetoPlay");
        return builder;
    }

    void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    boolean isMaintenanceMode() {
        return maintenance;
    }

    void logOnEntry() {
        appendToLog("entryOn");
    }

    void logOnExit() {
        appendToLog("exitOn");
    }

    void logOffEntry() {
        appendToLog("entryOff");
    }

    void logOffExit() {
        appendToLog("exitOff");
    }

    void logDabEntry() {
        appendToLog("entryDAB");
    }

    void logDabExit() {
        appendToLog("exitDAB");
    }

    void logFmEntry() {
        appendToLog("entryFM");
    }

    void logFmExit() {
        appendToLog("exitFM");
    }

    void logTransitionFromOffToOn() {
        appendToLog("OffToOn");
    }

    void logTransitionFromOnToOff() {
        appendToLog("OnToOff");
    }

    void logTransitionFromFmToDab() {
        appendToLog("FMtoDAB");
    }

    void logTransitionFromDabToFm() {
        appendToLog("DABtoFM");
    }

    void logTransitionFromOffToMaintenance() {
        appendToLog("OffToMaintenance");
    }

    void logTransitionFromMaintenanceToOff() {
        appendToLog("MaintenanceToOff");
    }

    void logMaintenanceEntry() {
        appendToLog("entryMaintenance");
    }

    void logMaintenanceExit() {
        appendToLog("exitMaintenance");
    }

    void logTransitionFromPlayToAutoTune() {
        appendToLog("PlayToAutoTune");
    }

    void logPlayEntry() {
        appendToLog("entryPlay");
    }

    void logPlayExit() {
        appendToLog("exitPlay");
    }

    void logTransitionFromAutoTuneToPlay() {
        appendToLog("AutoTuneToPlay");
    }

    void logAutoTuneEntry() {
        appendToLog("entryAutoTune");
    }

    void logAutoTuneExit() {
        appendToLog("exitAutoTune");
    }

    String consumeLog() {
        final String result = log.toString();
        log = new StringBuilder();
        return result;
    }

    private void appendToLog(String text) {
        if (log.length() > 0) {
            log.append('.');
        }
        log.append(text);
    }
}
