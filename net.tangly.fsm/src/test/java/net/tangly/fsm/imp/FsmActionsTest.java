/*
 * Copyright 2006-2021 Marcel Baumann
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
import net.tangly.fsm.State;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FsmActionsTest {
    enum States {
        Root, A, AA, AB, B, BA, BB
    }

    enum Events {
        A_A, A_B, A_BA, AA_AA, AA_AB, AA_B, AB_BA
    }

    @Test
    void fireInitialTransitions() {
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.A).add(States.AA, "State AA").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.addToRoot(States.B, "State A").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.B).add(States.BA, "State BA").onEntry(mockAction()).onExit(mockAction()).isInitial();

        var fsm = builder.machine("test", this);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.AA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.AA).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.B).entryAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.B).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BA).entryAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BA).exitAction(), times(0)).accept(eq(this), any());
    }

    @Test
    void fireLocalTransitionTest() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.A).onLocal(Events.A_A).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.A_A);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(0)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    @Test
    void fireTransitionToSameStateTest() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.A).on(Events.A_A).to(States.A).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.A_A);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(2)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(1)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    @Test
    void fireTransitionToAnotherStateTest() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.addToRoot(States.B, "State A").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.A).on(Events.A_B).to(States.B).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.A_B);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).exitAction(), times(0)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    @Test
    void fireTransitionToCompositeStateTest() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.addToRoot(States.B, "State A").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.B).add(States.BA, "State BA").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.B).add(States.BB, "State BA").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.A).on(Events.A_B).to(States.B).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.A_B);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.BA).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BB).entryAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BB).exitAction(), times(0)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    @Test
    void fireTransitionToStateWithInitialSubstate() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.A).add(States.AA, "State AA").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.addToRoot(States.B, "State A").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.B).add(States.BA, "State BA").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.AA).on(Events.AA_B).to(States.B).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.AA_B);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.AA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.AA).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.BA).exitAction(), times(0)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    @Test
    void fireTransitionToAnotherSubstateTest() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.addToRoot(States.B, "State A").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.B).add(States.BA, "State BA").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.A).on(Events.A_BA).to(States.BA).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.A_BA);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.BA).exitAction(), times(0)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    @Test
    void fireTransitionFromAndToSubstateTest() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.A).add(States.AB, "State AB").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.addToRoot(States.B, "State A").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.B).add(States.BA, "State BA").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.AB).on(Events.AB_BA).to(States.BA).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.AB_BA);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.AB).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.AB).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.BA).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.BA).exitAction(), times(0)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    @Test
    void fireTransitionFromAndToStateWithInitialSubstate() {
        var transitionAction = mockAction();
        FsmBuilder<FsmActionsTest, States, Events> builder = FsmBuilder.of(States.Root);
        builder.addToRoot(States.A, "State A").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.A).add(States.AA, "State AA").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.addToRoot(States.B, "State A").onEntry(mockAction()).onExit(mockAction());
        builder.in(States.B).add(States.BA, "State BA").onEntry(mockAction()).onExit(mockAction()).isInitial();
        builder.in(States.A).on(Events.A_B).to(States.B).execute(transitionAction).build();

        var fsm = builder.machine("test", this);
        var event = Event.of(Events.A_B);
        fsm.fire(event);

        verify(findBy(builder, States.A).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.A).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.AA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.AA).exitAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.B).exitAction(), times(0)).accept(eq(this), any());
        verify(findBy(builder, States.BA).entryAction(), times(1)).accept(eq(this), any());
        verify(findBy(builder, States.BA).exitAction(), times(0)).accept(eq(this), any());
        verify(transitionAction, times(1)).accept(this, event);
    }

    private static BiConsumer<FsmActionsTest, Event<Events>> mockAction() {
        return mock(BiConsumer.class);
    }

    private static State<FsmActionsTest, States, Events> findBy(@NotNull FsmBuilder<FsmActionsTest, States, Events> builder, @NotNull States stateId) {
        return builder.definition().findBy(stateId).orElseThrow();
    }
}
