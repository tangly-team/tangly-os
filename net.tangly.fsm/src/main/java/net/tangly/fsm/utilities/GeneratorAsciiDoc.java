/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.fsm.utilities;

import net.tangly.fsm.State;
import net.tangly.fsm.Transition;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

/**
 * Generates a AsciiDoc description of the finite state machine declaration.
 * A state has a name, a context, description, final flag, initial flag, composite flag, entry action, and exit action.
 * A transition has a start state name, end state name, description, local flag, guard, action,
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public class GeneratorAsciiDoc<O, S extends Enum<S>, E extends Enum<E>> extends Generator<O, S, E> {
    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     * @see Generator#Generator(FsmBuilder, String)
     */
    public GeneratorAsciiDoc(@NotNull FsmBuilder<O, S, E> builder, String name) {
        super(builder, name);
    }

    @Override
    public void generate(@NotNull PrintWriter writer) {
        generateImageXRef(writer);
        writeStateTablePreamble(writer);
        states.stream().sorted().forEach(state -> writeState(state, writer));
        writeTablePostamble(writer);
        writeTransitionTablePreamble(writer);
        states.stream().sorted().forEach(state -> writeTransitions(state, writer));
        writeTablePostamble(writer);
        writer.flush();
        writer.close();
    }

    @Override
    public String extension() {
        return ("adoc");
    }

    private void generateImageXRef(@NotNull PrintWriter writer) {
        writer.append("== ").append(name).append(" Finite State Machine").println();
        writer.println();
        writer.append("image::pics/").append(name).append(".svg[").append(name).append("]").println();
        writer.println();
    }

    private void writeStateTablePreamble(@NotNull PrintWriter writer) {
        writer.append("=== ").append(name).append(" States").println();
        writer.println();
        writer.println("[cols=\"2,2,3,1,1,1,3,3\"]");
        writer.println("|===");
        writer.println("|Name |Context |Description |Final |Initial |Composite |Entry Action |Exit Action");
        writer.println();
    }

    private static void writeTablePostamble(@NotNull PrintWriter writer) {
        writer.println("|===");
        writer.println();
    }

    private void writeTransitionTablePreamble(@NotNull PrintWriter writer) {
        writer.append("=== ").append(name).append(" Transitions").println();
        writer.println();
        writer.println("[cols=\"2,2,3,1,3,3\"]");
        writer.println("|===");
        writer.println("|Start State |End State |Description |Local |Guard |Action");
        writer.println();
    }

    private void writeState(@NotNull State<O, S, E> state, @NotNull PrintWriter writer) {
        writer.append("|");
        appendInlineAnchor(writer, state).append(getStateName(state)).println();
        writer.append("|").println(findOwner(state).map(this::getStateName).orElse("-"));
        writer.append("|").println(toString(state.description()));
        writer.append("|").println(state.isFinal());
        writer.append("|").println(state.isInitial());
        writer.append("|").println(state.isComposite());
        writer.append("|").println((state.entryAction() != null) ? toString(state.entryActionDescription()) : "-");
        writer.append("|").println((state.exitAction() != null) ? toString(state.exitActionDescription()) : "-");
        writer.println();
    }

    private void writeTransitions(@NotNull State<O, S, E> state, @NotNull PrintWriter writer) {
        state.localTransitions().stream().sorted(transitionComparator()).forEach(o -> writeTransition(o, true, writer));
        state.transitions().stream().sorted(transitionComparator()).forEach(o -> writeTransition(o, false, writer));
    }

    private @NotNull PrintWriter appendInlineAnchor(@NotNull PrintWriter writer, @NotNull State<O, S, E> state) {
        writer.append("[[").append(name).append("-").append(getStateName(state)).append("]]");
        return writer;
    }

    private @NotNull PrintWriter appendInternalXRef(@NotNull PrintWriter writer, @NotNull State<O, S, E> state) {
        writer.append("<<").append(name).append("-").append(getStateName(state)).append(",").append(getStateName(state)).append(">>");
        return writer;
    }


    private void writeTransition(@NotNull Transition<O, S, E> transition, boolean isLocal, @NotNull PrintWriter writer) {
        writer.append("|");
        appendInternalXRef(writer, transition.source()).println();

        writer.append("|");
        appendInternalXRef(writer, transition.target()).println();

        writer.append("|").append(toString(transition.description())).println();
        writer.append("|").append(Boolean.toString(isLocal)).println();
        writer.append("|").append(toString(transition.guardDescription())).println();
        writer.append("|").append(toString(transition.actionDescription())).println();
        writer.println();
    }
}
