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

import net.tangly.fsm.utilities.StaticChecker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests the static checkers for finite state machines.
 */
class StaticCheckerTest {

    /**
     * The finite state machine internal states for the test configuration.
     */
    enum States {
        ROOT, A, AA, AB, B
    }

    /**
     * The finite state machine internal events for the test configuration.
     */
    enum Events {
        A_B
    }

    @Test
    void checkStateIdUsedOnceIllegalArgumentExceptionTest() {
        DefinitionBuilder<StaticCheckerTest, States, Events> builder = new DefinitionBuilder<>(States.ROOT);
        builder.root().add(States.A).isInitial(true);
        builder.in(States.A).add(States.AA).isInitial(true);
        builder.in(States.A).add(States.AB);
        builder.root().add(States.B);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(()-> builder.in(States.B).add(States.AB));
    }

    @Test
    void checkStateIdUsedOnceTest() {
        StateImp<StaticCheckerTest, States, Events> root = new StateImp<>(States.A);
        root.addSubstate(new StateImp<>(States.AA));
        root.addSubstate(new StateImp<>(States.AA));
        StaticChecker<StaticCheckerTest, States, Events> checker = new StaticChecker<>();
        assertThat(checker.checkStateIdUsedOnce(root).size()).isEqualTo(4);
    }


    @Test
    void checkStateHasAtMostOneInitialStateTest() {
        DefinitionBuilder<StaticCheckerTest, States, Events> builder = new DefinitionBuilder<>(States.ROOT);
        builder.root().add(States.A).isInitial(true);
        builder.in(States.A).add(States.AA).isInitial(true);
        builder.in(States.A).add(States.AB).isInitial(true);
        StaticChecker<StaticCheckerTest, States, Events> checker = new StaticChecker<>();
        assertThat(checker.checkStateHasAtMostOneInitialState(builder.definition()).size()).isEqualTo(1);
    }

    @Test
    void checkStateWithAfferentTransitionHasInitialStateTest() {
        DefinitionBuilder<StaticCheckerTest, States, Events> builder = new DefinitionBuilder<>(States.ROOT);
        builder.root().add(States.A).isInitial(true);
        builder.root().add(States.B);
        builder.in(States.B).add(States.AB);
        builder.in(States.A).on(Events.A_B).to(States.B);
        StaticChecker<StaticCheckerTest, States, Events> checker = new StaticChecker<>();
        assertThat(checker.checkStateWithAfferentTransitionHasInitialState(builder.definition()).size()).isEqualTo(1);
    }
}
