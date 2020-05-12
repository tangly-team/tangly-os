/*
 * Copyright 2006-2020 Marcel Baumann
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract generator to create a human-readable description of a finite state machine description.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public abstract class Generator<O, S extends Enum<S>, E extends Enum<E>> {
    private static final int INDENTATION = 4;
    private final Comparator<Transition<O, S, E>> comparator;
    protected final FsmBuilder<O, S, E> builder;
    protected final String name;
    protected final Set<State<O, S, E>> states;

    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     */
    public Generator(@NotNull FsmBuilder<O, S, E> builder, @NotNull String name) {
        this.builder = builder;
        this.name = name;
        this.comparator = Comparator.comparing(Transition<O, S, E>::source).thenComparing(Transition::target).thenComparing(Transition::eventId)
                .thenComparing(Comparator.nullsLast(Comparator.comparing(Transition::guardDescription)));
        this.states = new HashSet<>();
        getAllStates(this.states, builder.definition());
    }

    /**
     * Generates the content of the file identified through the path if the file does not exist or the new creation of the output is different to the
     * one in the file. This feature is for example helpful if the file is under version control to avoid spurious changes.
     *
     * @param path path to the file to updated.
     * @return flag indicating if the file was updated or not
     * @throws IOException if a file could not be read or written
     */
    public boolean generateFileIfChanged(@NotNull Path path) throws IOException {
        var writer = new StringWriter();
        generate(new PrintWriter(writer));
        String newText = writer.toString();
        Path filePath = Paths.get(path.toString(), name + "." + extension());
        boolean shouldBeUpdated;
        if (Files.exists(filePath)) {
            String oldText = Files.readString(filePath, StandardCharsets.UTF_8);
            shouldBeUpdated = !oldText.contentEquals(newText);
        } else {
            shouldBeUpdated = true;
        }
        if (shouldBeUpdated) {
            try (PrintWriter out = new PrintWriter(filePath.toFile(), StandardCharsets.UTF_8)) {
                out.append(newText);
                out.flush();
            }
        }
        return shouldBeUpdated;
    }

    /**
     * Implements the creation of the text content for a specific generator.
     *
     * @param writer writer to which the content shall be written
     */
    public abstract void generate(@NotNull PrintWriter writer);

    /**
     * Returns the extension used by the generator when writing its output.
     *
     * @return extension used by the generator
     */
    public abstract String extension();

    protected @NotNull PrintWriter indent(@NotNull PrintWriter writer, int depth) {
        return writer.append(" ".repeat(INDENTATION * depth));
    }

    protected @NotNull String getStateId(@NotNull State<O, S, E> state) {
        return Integer.toString(state.id().ordinal());
    }


    protected @NotNull String getStateName(@NotNull State<O, S, E> state) {
        return state.id().name();
    }

    protected @NotNull String toString(String value) {
        return (value != null) ? value.trim() : "";
    }

    protected @NotNull Comparator<Transition<O, S, E>> transitionComparator() {
        return comparator;
    }

    protected Optional<State<O, S, E>> findOwner(State<O, S, E> state) {
        return states.stream().filter(o -> o.substates().contains(state)).findAny();
    }

    private void getAllStates(Set<State<O, S, E>> states, State<O, S, E> state) {
        states.add(state);
        state.substates().forEach(o -> getAllStates(states, o));
    }
}
