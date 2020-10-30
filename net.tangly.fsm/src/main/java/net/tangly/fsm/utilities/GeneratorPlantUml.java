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

package net.tangly.fsm.utilities;

import java.io.PrintWriter;

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
public class GeneratorPlantUml<O, S extends Enum<S>, E extends Enum<E>> extends Generator<O, S, E> {
    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     * @see Generator#Generator(FsmBuilder, String)
     */
    public GeneratorPlantUml(@NotNull FsmBuilder<O, S, E> builder, String name) {
        super(builder, name);
    }

    @Override
    public void generate(@NotNull PrintWriter writer) {
        writePreamble(writer);
        writeState(builder.definition(), 0, writer);
        writePostamble(writer);
        writer.flush();
        writer.close();
    }

    @Override
    public String extension() {
        return ("puml");
    }


    private static void writePreamble(@NotNull PrintWriter writer) {
        writer.append("@startuml").println();
        writer.append("hide empty description").println();
        writer.println();
    }

    private static void writePostamble(@NotNull PrintWriter writer) {
        writer.println();
        writer.append("@enduml").println();
    }

    private void writeState(@NotNull State<O, S, E> state, int depth, @NotNull PrintWriter writer) {
        if (state.isInitial() && (state != builder.definition())) {
            indent(writer, depth).append("[*] --> ").append(getStateName(state)).println();
        }
        if (state.isComposite()) {
            indent(writer, depth).append("state ").append(getStateName(state)).println(" {");
            state.substates().stream().sorted().forEach(o -> writeState(o, depth + 1, writer));
            writeTransitions(state, writer, depth + 1);
            writer.println("}");
        } else {
            indent(writer, depth).append("state ").append(getStateName(state)).println();
            writeTransitions(state, writer, depth);
        }
        writer.println();
    }

    private void writeTransitions(@NotNull State<O, S, E> state, @NotNull PrintWriter writer, int depth) {
        if (state.isFinal() && (state != builder.definition())) {
            indent(writer, depth).append(getStateName(state)).append(" --> [*]").println();
        }
        state.transitions().stream().sorted(transitionComparator()).forEach(transition -> {
            var source = transition.source();
            var target = transition.target();
            indent(writer, depth).append(getStateName((source))).append(" -> ").append(getStateName((target))).append(" : ")
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
