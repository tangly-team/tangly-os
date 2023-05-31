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
import net.tangly.fsm.StateMachineEventHandler;
import net.tangly.fsm.dsl.FsmBuilder;
import net.tangly.fsm.utilities.DynamicChecker;
import net.tangly.fsm.utilities.StateMachineLogger;
import net.tangly.fsm.utilities.StaticChecker;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * The test class verifies the expected behavior of hierarchical machine declaration, instantiation of finite state machines, and firing of various transitions.
 */
@SuppressWarnings("unchecked")
public class FsmTest {
    /**
     * The finite state machine internal states for the test configuration.
     */
    enum States {
        Root, A, AA, AB, B, BA, BB, C
    }

    /**
     * The finite state machine internal events for the test configuration.
     */
    enum Events {
        A_A, A_C, AA_AA, AA_AB, AA_B, AA_BB, AB_AA, AB_AB, B_C, BA_A, BA_BB, BB_BA, BB_BB, BB_C, C_C
    }

    /**
     * Creates a finite state machine declaration.
     *
     * @return the definition of the finite state machine
     */
    @SuppressWarnings("unchecked")
    public static FsmBuilder<FsmTest, States, Events> build() {
        return build(mock(BiConsumer.class));
    }

    /**
     * Creates a finite state machine declaration.
     *
     * @param action action to be executed when firing a transition
     * @return the definition of the finite state machine
     */
    private static FsmBuilder<FsmTest, States, Events> build(BiConsumer<FsmTest, Event<Events>> action) {
        FsmBuilder<FsmTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").isInitial();
        builder.in(States.A).add(States.AA, "State AA").isInitial();
        builder.in(States.A).add(States.AB, "State AB");
        builder.addToRoot(States.B).hasHistory();
        builder.in(States.B).add(States.BA, "State BA").hasHistory();
        builder.in(States.B).add(States.BB, "State BB").isInitial();
        builder.addToRoot(States.C);
        builder.in(States.A).on(Events.A_C).to(States.C, "A -> C when A_C").execute(action).build();
        builder.in(States.AA).on(Events.AA_AB).to(States.AB, "AA -> AB when AA_AB").execute(action).build();
        builder.in(States.AA).on(Events.AA_B).to(States.B, "AA -> B when AA_B").execute(action).build();
        builder.in(States.AA).on(Events.AA_BB).to(States.BB, "AA -> BB when (AA_BB)").execute(action).build();
        builder.in(States.AA).onLocal(Events.AA_AA, "Local transition AA -> AA when (AA_AA)").execute(action).build();
        builder.in(States.AB).on(Events.AB_AA).to(States.AA, "AB -> AA when AB_AA").execute(action).build();
        builder.in(States.AB).on(Events.AB_AB).to(States.AB, "AB -> AB when AB_AB").execute(action).build();
        builder.in(States.B).on(Events.B_C).to(States.C, "B -> C when B_C").execute(action).build();
        builder.in(States.BA).on(Events.BA_A).to(States.A, "BA -> A when BA_C").execute(action).build();
        builder.in(States.BA).on(Events.BA_BB).to(States.BB, "BA -> BB when BA_BB").execute(action).build();
        builder.in(States.BB).on(Events.BB_BA).to(States.BA).execute(action, "VV -> BA when BB_BA").build();
        builder.in(States.BB).onLocal(Events.BB_BB, "Local transition BB -> BB when BB_BB").execute(action).build();
        builder.in(States.BB).on(Events.BB_C).to(States.C, "BB -> C when BB_C").execute(action).build();
        return builder;
    }

    /**
     * Creates a finite state machine declaration.
     *
     * @return the definition of the finite state machine
     */
    static FsmBuilder<FsmTest, States, Events> buildWithException() {
        FsmBuilder<FsmTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A).isInitial();
        builder.addToRoot(States.C).onEntry((o, e) -> {
            throw new RuntimeException();
        });
        builder.in(States.A).on(Events.A_C).to(States.C).execute((o, e) -> {
            throw new RuntimeException();
        }).build();
        builder.in(States.A).onLocal(Events.AA_AA).onlyIf((o, e) -> {
            throw new RuntimeException();
        }).execute((o, e) -> {
            throw new RuntimeException();
        }).build();
        builder.in(States.C).onLocal(Events.C_C).execute((o, e) -> {
            throw new RuntimeException();
        }).build();
        return builder;
    }

    /**
     * Tests the construction of state machine using the fluent build interface.
     */
    @Test
    void buildFsmStatesTest() {
        var root = build().definition();
        assertThat(root.getStateFor(States.Root)).isNotNull();
        assertThat(root.isInitial()).isTrue();
        assertThat(root.substates()).hasSize(3);
        assertThat((root.entryAction())).isNull();
        assertThat(root.exitAction()).isNull();
        assertThat(root.toString()).contains("id=Root");
        assertThat(root.toString()).contains("hasHistory=false");
        assertThat(root.toString()).contains("initial=true");
        assertThat(root.getStateFor(States.A)).isNotNull();
        assertThat(root.getStateFor(States.A).substates()).hasSize(2);
        assertThat(root.getStateFor(States.A).isFinal()).isFalse();
        assertThat(root.getStateFor(States.B)).isNotNull();
        assertThat(root.getStateFor(States.B).substates()).hasSize(2);
        assertThat(root.getStateFor(States.B).hasHistory()).isTrue();
        assertThat(root.getStateFor(States.B).isFinal()).isFalse();
        assertThat(root.getStateFor(States.C)).isNotNull();
        assertThat(root.getStateFor(States.C).substates()).isEmpty();
        assertThat(root.getStateFor(States.C).isFinal()).isTrue();
        assertThat(root.getStateFor(States.A).transitions()).isNotEmpty();
        assertThat(root.getStateFor(States.A).transitions().stream().findAny().orElseThrow().toString()).contains("A ->");
        assertThat(root.getStateFor(States.A).transitions().stream().findAny().orElseThrow().toString()).contains("-> C");
        assertThat(root.getStateFor(States.A).getStateFor(States.AA)).isNotNull();
        assertThat(root.getStateFor(States.A).getStateFor(States.AB)).isNotNull();
        assertThat(root.getStateFor(States.A).getStateFor(States.BA)).isNull();
        assertThat(root.getStateFor(States.B).getStateFor(States.BA)).isNotNull();
        assertThat(root.getStateFor(States.B).getStateFor(States.BB)).isNotNull();
        assertThat(root.getStateFor(States.B).getStateFor(States.AB)).isNull();
        assertThat(root.getStateFor(States.AA).substates()).isEmpty();
        assertThat(root.getStateFor(States.AA).isInitial()).isTrue();
        assertThat(root.getStateFor(States.AB).substates()).isEmpty();
        assertThat(root.getStateFor(States.BA).substates()).isEmpty();
        assertThat(root.getStateFor(States.BB).substates()).isEmpty();
    }

    /**
     * Tests the initialization of the machine with the defined initial states.
     */
    @Test
    void buildFsmInitialStatesTest() {
        var root = build().definition();
        var initialStates = root.initialStates();
        assertThat(initialStates).hasSize(3);
        var state = Objects.requireNonNull(initialStates.pollFirst());
        assertThat(state.isInitial() && (state.id() == States.Root)).isTrue();
        state = Objects.requireNonNull(initialStates.pollFirst());
        assertThat(state.isInitial() && (state.id() == States.A)).isTrue();
        state = Objects.requireNonNull(initialStates.pollFirst());
        assertThat(state.isInitial() && (state.id() == States.AA)).isTrue();
    }

    @Test
    void buildFsmWrongStateDeclarationsTest() {
        FsmBuilder<FsmTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").isInitial();
        builder.in(States.A).add(States.AA, "State AA").isInitial();
        assertThrows(IllegalArgumentException.class, () -> builder.addToRoot(States.A));
        assertThrows(IllegalArgumentException.class, () -> builder.addToRoot(States.AA));
        assertThrows(IllegalArgumentException.class, () -> builder.in(States.A).add(States.AA));
        assertThrows(IllegalArgumentException.class, () -> builder.in(States.B).add(States.AA));
    }

    @Test
    void buildFsmWrongTransitionsTest() {
        FsmBuilder<FsmTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").isInitial();
        assertThrows(IllegalArgumentException.class, () -> builder.in(States.A).on(Events.A_C).to(States.C));
    }

    @Test
    void buildMachineTest() {
        var root = build().definition();
        var fsm = new StateMachineImp<>("test-fsm", root, this);

        assertThat(fsm.name()).isEqualTo("test-fsm");
        assertThat(fsm.toString()).isNotNull();
    }

    @Test
    void fireTransitionTest() {
        var action = mock(BiConsumer.class);
        var fsm = new StateMachineImp<FsmTest, FsmTest.States, FsmTest.Events>("test-fsm", build(action).definition(), this);

        assertThat(fsm.historyStates()).isEmpty();
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmTest.States.A));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmTest.States.AA));
        assertThat(fsm.activeStates()).doesNotContain(fsm.root().getStateFor(FsmTest.States.AB));
        var event = Event.of(FsmTest.Events.AA_AB);
        fsm.fire(event);
        assertThat(fsm.historyStates()).isEmpty();
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmTest.States.A));
        assertThat(fsm.activeStates()).doesNotContain(fsm.root().getStateFor(FsmTest.States.AA));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(FsmTest.States.AB));
        assertThat(fsm.historyStates()).isEmpty();
        verify(action, times(1)).accept(this, event);
    }


    /**
     * Tests the transition from a containing state to a final state.
     */
    @Test
    void fireFatherTransitionTest() {
        var action = mock(BiConsumer.class);
        var fsm = new StateMachineImp<FsmTest, States, Events>("test-fsm", build(action).definition(), this);
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.A));
        assertThat(fsm.root().initialStates()).contains(fsm.root().getStateFor(States.AA));
        assertThat(fsm.historyStates()).isEmpty();
        var event = Event.of(Events.A_C);
        fsm.fire(event);
        assertThat(fsm.activeStates()).doesNotContain(fsm.root().getStateFor(States.A));
        assertThat(fsm.activeStates()).doesNotContain(fsm.root().getStateFor(States.AA));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.C));
        assertThat(fsm.historyStates()).isEmpty();
        verify(action, times(1)).accept(this, event);
    }

    /**
     * Tests the transition from a substate to a composite state containing an initial substate.
     */
    @Test
    void fireTransitionToStateWithInitialStatesTest() {
        var action = mock(BiConsumer.class);
        var fsm = new StateMachineImp<FsmTest, States, Events>("test-fsm", build(action).definition(), this);
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.A));
        assertThat(fsm.root().initialStates()).contains(fsm.root().getStateFor(States.AA));
        assertThat(fsm.historyStates()).isEmpty();
        fsm.fire(Event.of(Events.AA_B));
        assertThat(fsm.activeStates()).doesNotContain(fsm.root().getStateFor(States.A));
        assertThat(fsm.activeStates()).doesNotContain(fsm.root().getStateFor(States.AA));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.B));
        assertThat(fsm.activeStates()).contains(fsm.root().getStateFor(States.BB));
        assertThat(fsm.historyStates()).isEmpty();
    }

    /**
     * Tests the hooking of an event handler into a finite state machine.
     */
    @Test
    void eventHandlerTest() {
        var action = mock(BiConsumer.class);
        var root = build(action).definition();
        var fsm = new StateMachineImp<FsmTest, States, Events>("test-fsm", root, this);
        StateMachineEventHandler<FsmTest, States, Events> handler = mock(StateMachineEventHandler.class);
        DynamicChecker<FsmTest, States, Events> checker = new DynamicChecker<>(fsm);
        assertThat(fsm.isRegistered(handler)).isFalse();
        assertThat(fsm.isRegistered(checker)).isFalse();
        fsm.addEventHandler(handler);
        fsm.addEventHandler(checker);
        assertThat(fsm.isRegistered(handler)).isTrue();
        assertThat(fsm.isRegistered(checker)).isTrue();
        fsm.reset();
        verify(handler, times(1)).wasReset();
        fsm.fire(Event.of(Events.AA_AB));
        fsm.fire(Event.of(Events.AB_AB));
        fsm.fire(Event.of(Events.AB_AA));
        fsm.fire(Event.of(Events.AA_AA));
        fsm.fire(Event.of(Events.AA_B));
        fsm.fire(Event.of(Events.BA_BB));
        fsm.fire(Event.of(Events.B_C));
        fsm.removeEventHandler(handler);
        fsm.removeEventHandler(checker);
        assertThat(fsm.isRegistered(handler)).isFalse();
        assertThat(fsm.isRegistered(checker)).isFalse();
    }

    @Test
    void eventHandlerExceptionTest() {
        var root = buildWithException().definition();
        var fsm = new StateMachineImp<>("test-fsm", root, this);
        var handler = mock(StateMachineEventHandler.class);
        var logger = new StateMachineLogger<>(fsm);
        assertThat(fsm.isRegistered(handler)).isFalse();
        assertThat(fsm.isRegistered(logger)).isFalse();
        fsm.addEventHandler(handler);
        fsm.addEventHandler(logger);
        assertThat(fsm.isRegistered(handler)).isTrue();
        assertThat(fsm.isRegistered(logger)).isTrue();
        fsm.reset();
        verify(handler, times(1)).wasReset();
        fsm.fire(Event.of(Events.AA_AA));
        fsm.fire(Event.of(Events.A_C));
        fsm.fire(Event.of(Events.C_C));
    }

    @Test
    void dynamicCheckerExceptionTest() {
        var root = buildWithException().definition();
        var fsm = new StateMachineImp<>("test-fsm", root, this);
        var checker = new DynamicChecker<>(fsm);
        assertThat(fsm.isRegistered(checker)).isFalse();
        fsm.addEventHandler(checker);
        assertThat(fsm.isRegistered(checker)).isTrue();
        fsm.reset();
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> fsm.fire(Event.of(Events.AA_AA)));
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> fsm.fire(Event.of(Events.A_C)));
    }

    /**
     * Tests a static checker against our final state machine.
     */
    @Test
    void staticCheckerTest() {
        FsmBuilder<FsmTest, States, Events> builder = build();
        StaticChecker<FsmTest, States, Events> checker = new StaticChecker<>();
        assertThat(checker.check(builder.definition())).isEmpty();
        assertThat(checker.checkStateHasAtMostOneInitialState(builder.definition())).isEmpty();
        assertThat(checker.checkStateIdUsedOnce(builder.definition())).isEmpty();
        assertThat(checker.checkStateWithAfferentTransitionHasInitialState(builder.definition())).isEmpty();
    }
}
