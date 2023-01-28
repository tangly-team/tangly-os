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

package net.tangly.fsm.utilities;

import net.tangly.fsm.State;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Objects;

public class GeneratorStateMachineCat<O, S extends Enum<S>, E extends Enum<E>> extends Generator<O, S, E> {
    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     * @see Generator#Generator(FsmBuilder, String)
     */
    public GeneratorStateMachineCat(@NotNull FsmBuilder<O, S, E> builder, String name) {
        super(builder, name);
    }

    @Override
    public void generate(@NotNull PrintWriter writer) {
        writeState(builder.definition(), 0, writer, ";");
    }

    @Override
    public String extension() {
        return ("smcat");
    }

    private void writeState(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer, String terminator) {
        if (state.isComposite()) {
            if (!state.equals(builder.definition())) {
                writeInitialFinalStates(state, depth, writer);
            }
            indent(writer, depth).append(state.id().name()).append(" {").println();
            writeEntryExitActions(state, depth, writer);
            if (state.hasHistory()) {
                indent(writer, depth + 1).append(state.id().name()).append(".history,").println();
            }
            var substates = state.substates().stream().sorted(Comparator.comparing(State::id)).toList();
            for (var substate : substates) {
                writeState(substate, depth + 1, writer, (substates.get(substates.size() - 1).equals(substate)) ? ";" : ",");
            }
            writer.println();
            writeInitialFinalTransitions(state, depth + 1, writer);
            writeTransitions(state, depth + 1, writer);
            state.substates().stream().sorted(Comparator.comparing(State::id)).forEach(o -> writeTransitions(o, depth + 1, writer));
            indent(writer, depth).append("}").append(terminator).println();
        } else {
            if ((state.entryAction() != null) || (state.exitAction() != null)) {
                indent(writer, depth).append(state.id().name()).append(":").println();
                writeEntryExitActions(state, depth, writer);
                indent(writer, depth).println(";");
            } else {
                indent(writer, depth).append(state.id().name()).append(terminator).println();
            }
        }
    }

    private void writeInitialFinalStates(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        if (state.isInitial()) {
            indent(writer, depth).append(state.id().name()).append(".initial,").println();
        }
        if (state.isFinal()) {
            indent(writer, depth).append(state.id().name()).append(".final,").println();
        }
    }

    private void writeInitialFinalTransitions(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        if (state.isInitial()) {
            indent(writer, depth).append(state.id().name()).append(".initial -> ").append(state.id().name()).append(";").println();
        }
        if (state.isFinal()) {
            indent(writer, depth).append(state.id().name()).append(" -> ").append(state.id().name()).append(".final;").println();
        }
    }

    private void writeEntryExitActions(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        if (state.entryAction() != null) {
            indent(writer, depth + 1).append("entry/ ").append(Objects.nonNull(state.entryActionDescription()) ? state.entryActionDescription() : "-")
                .println();
        }
        if (state.exitAction() != null) {
            indent(writer, depth + 1).append("exit/ ").append(Objects.nonNull(state.exitActionDescription()) ? state.exitActionDescription() : "-").println();
        }
    }

    private void writeTransitions(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        state.transitions().stream().sorted(transitionComparator()).forEach(transition -> {
            var source = transition.source();
            var target = transition.target();
            indent(writer, depth).append(getStateName((source))).append(" -> ").append(getStateName((target))).append(": ").append(transition.eventId().name());
            if (transition.hasGuard()) {
                writer.append(" [").append(transition.guardDescription()).append("]");
            }
            if (transition.hasAction()) {
                writer.append(" / ").append(transition.actionDescription() != null ? transition.actionDescription() : "");
            }
            writer.println(";");
        });
    }
}
