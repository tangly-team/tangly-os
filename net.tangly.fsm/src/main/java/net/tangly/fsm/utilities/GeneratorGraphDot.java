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

import java.io.PrintWriter;
import java.util.Comparator;

import net.tangly.fsm.State;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * The generator creates graphical graph representations of a finite state machine in the graphical language of <a href="http://www.graphviz.org/">Graphviz
 * dot</a> language.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public class GeneratorGraphDot<O, S extends Enum<S>, E extends Enum<E>> extends Generator<O, S, E> {
    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     * @see Generator#Generator(FsmBuilder, String)
     */
    public GeneratorGraphDot(@NotNull FsmBuilder<O, S, E> builder, String name) {
        super(builder, name);
    }

    @Override
    public void generate(@NotNull PrintWriter writer) {
        try (writer) {
            writePreamble(writer);
            writeState(builder.definition(), 1, writer);
            writer.println();
            states.stream().sorted(Comparator.comparing(State::id)).forEach(state -> writeTransitions(state, writer));
            writePostamble(writer);
            writer.flush();
        }
    }

    @Override
    public String extension() {
        return ("dot");
    }


    private void writePreamble(@NotNull PrintWriter writer) {
        writer.append("digraph G {").println();
        indent(writer, 1).append("compound=true;\n").println();
    }

    private static void writePostamble(@NotNull PrintWriter writer) {
        writer.append("}").println();
    }

    private void writeState(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        if (state.isComposite()) {
            indent(writer, depth).append("subgraph \"cluster-").append(state.id().name()).println("\" {");
            indent(writer, depth + 1).append(getStyle(state)).println(";");
            indent(writer, depth + 1).append("label = \"").append(state.id().name()).println("\"");
            writer.println();
            state.substates().stream().sorted(Comparator.comparing(State::id)).forEach(o -> writeState(o, depth + 1, writer));
            indent(writer, depth).println("}");
        } else {
            indent(writer, depth).append(state.id().name()).append(" [").append(getStyle(state)).println("];");
        }
    }

    private void writeTransitions(@NotNull State<O, S, E> state, @NotNull PrintWriter writer) {
        state.transitions().stream().sorted(transitionComparator()).forEach(transition -> {
            var source = transition.source();
            var target = transition.target();
            indent(writer, 1).append(getStateName((inferTransitionState(source)))).append(" -> ").append(getStateName((inferTransitionState(target))))
                .append(" [");
            if (source.isComposite()) {
                writer.append("ltail=\"cluster-").append(getStateName(source)).append("\", ");
            }
            if (target.isComposite()) {
                writer.append("lhead=\"cluster-").append(getStateName(target)).append("\", ");
            }
            writer.append("label=\"").append(transition.eventId().name());
            if (transition.hasGuard()) {
                writer.append("[").append(transition.guardDescription() != null ? transition.guardDescription() : "").append("]");
            }
            if (transition.hasAction()) {
                writer.append("/").append(transition.actionDescription() != null ? transition.actionDescription() : "");
            }
            writer.println("\"];");
        });
    }

    private State<O, S, E> inferTransitionState(@NotNull State<O, S, E> state) {
        return state.isComposite() ? inferTransitionState(state.substates().stream().sorted(Comparator.comparing(State::id)).findFirst().orElseThrow()) : state;
    }

    private String getStyle(@NotNull State<O, S, E> state) {
        StringBuilder buffer = new StringBuilder();
        String separator = "";
        buffer.append("style=\"");
        if (state.isComposite()) {
            buffer.append(separator).append("visible");
            separator = ", ";
        }
        if (state.isInitial()) {
            buffer.append(separator).append("dashed");
            separator = ", ";
        }
        if (state.isFinal()) {
            buffer.append(separator).append("bold");
            separator = ", ";
        }
        if (state.hasHistory()) {
            buffer.append(separator).append("filled");
            separator = ", ";
        }
        buffer.append("\"");
        return (separator.equals(", ") ? buffer.toString() : "");
    }
}
