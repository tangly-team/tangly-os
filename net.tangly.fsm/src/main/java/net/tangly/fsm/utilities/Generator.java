package net.tangly.fsm.utilities;

import net.tangly.fsm.State;
import net.tangly.fsm.Transition;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Abstract generator to create a human-readable description of a finite state machine description.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> enumeration type for the identifiers of states
 * @param <E> enumeration type for the identifiers of events
 */
public abstract class Generator<O, S extends Enum<S>, E extends Enum<E>> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Generator.class);
    protected static final int INDENTATION = 2;
    protected final FsmBuilder<O, S, E> builder;
    protected final String name;
    private final Comparator<Transition<O, S, E>> comparator;

    /**
     * Constructor of the class.
     *
     * @param builder the finite state machine builder containing the machine to draw
     * @param name    name of the finite state machine description
     */
    public Generator(@NotNull FsmBuilder<O, S, E> builder, String name) {
        this.builder = builder;
        this.name = name;
        this.comparator = Comparator.comparing(Transition<O, S, E>::source).thenComparing(Transition::target).thenComparing(Transition::eventId)
                .thenComparing(Comparator.nullsLast(Comparator.comparing(Transition::guardDescription)));
    }

    /**
     * Generates the content of the file identified through the path if the file does not exist or the new creation of the output is different to
     * the one in the file. This feature is for example helpful if the file is under version control to avoid spurious changes.
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
            String oldText = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
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

    protected @NotNull PrintWriter indent(@NotNull PrintWriter writer, int spaces) {
        return writer.append(" ".repeat(spaces));
    }

    protected @NotNull String getStateId(@NotNull State<O, S, E> state) {
        return Integer.toString(state.getId().ordinal());
    }


    protected @NotNull String getStateName(@NotNull State<O, S, E> state) {
        return state.getId().name();
    }

    protected @NotNull String toString(String value) {
        return (value != null) ? value.trim() : "";
    }

    protected @NotNull Comparator<Transition<O, S, E>> transitionComparator() {
        return comparator;
    }
}
