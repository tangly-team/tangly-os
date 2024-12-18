/*
 * Copyright 2023-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.fsm.utilities;

import net.tangly.fsm.State;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.function.Predicate;

public class GeneratorMermaid<O, S extends Enum<S>, E extends Enum<E>> extends Generator<O, S, E> {
    private static final String ARROW = " --> ";

    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     * @see Generator#Generator(FsmBuilder, String)
     */
    public GeneratorMermaid(@NotNull FsmBuilder<O, S, E> builder, String name) {
        super(builder, name);
    }

    @Override
    public void generate(@NotNull PrintWriter writer) {
        try (writer) {
            writer.append("stateDiagram-v2").println();
            writeState(builder.definition(), 0, writer);
            writer.flush();
        }
    }

    @Override
    public String extension() {
        return ("mmd");
    }

    private void writeState(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        if (state.isInitial() && (state != builder.definition())) {
            indent(writer, depth).append("[*]").append(ARROW).append(getStateName(state)).println();
        }
        if (state.isComposite()) {
            indent(writer, depth).append("state ").append(getStateName(state)).println(" {");
            state.substates().stream().sorted(Comparator.comparing(State::id)).filter(Predicate.not(State::isComposite))
                .forEach(o -> writeState(o, depth + 1, writer));
            state.substates().stream().sorted(Comparator.comparing(State::id)).filter(State::isComposite).forEach(o -> writeState(o, depth + 1, writer));
            if (state.isFinal() && (state != builder.definition())) {
                indent(writer, depth).append(getStateName(state)).append(ARROW).append("[*]").println();
            }
        } else {
            indent(writer, depth).append(getStateName(state)).println();
        }
        if (state.isFinal() && (state != builder.definition())) {
            indent(writer, depth).append(getStateName(state)).append("  --> [*]").println();
        }
        if (state.isComposite()) {
            indent(writer, depth).println("}");
        }
        writeTransitions(state, writer, depth);
        writer.println();
    }

    private void writeTransitions(@NotNull State<O, S, E> state, @NotNull PrintWriter writer, int depth) {
        state.transitions().stream().sorted(transitionComparator()).forEach(transition -> {
            var source = transition.source();
            var target = transition.target();
            indent(writer, depth).append(getStateName((source))).append(ARROW).append(getStateName((target))).append(" : ")
                .append(transition.eventId().toString());
            if (transition.hasGuard()) {
                writer.append(" [").append(transition.guardDescription() != null ? transition.guardDescription() : "").append("]");
            }
            if (transition.hasAction() && (transition.actionDescription() != null)) {
                writer.append(" / ").append(transition.actionDescription() != null ? transition.actionDescription() : "");
            }
            writer.println();
        });
    }
}
