package net.tangly.fsm.imp;

import net.tangly.fsm.utilities.GeneratorAsciiDoc;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratorAsciiDocTest {
    private final String dir = "/Users/Shared/Projects/tangly-os/net.tangly.fsm/src/site";

    /**
     * Tests the generation of the bbv finite state machine.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateGraphFsmTest() throws IOException {
        var generator = new GeneratorAsciiDoc<>(FsmBbv.build(), "fsm-bbv");
        assertThat(generator.generateFileIfChanged(Paths.get(dir))).isFalse();
    }

    /**
     * Tests the generation of the builder finite state machine.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateGraphFsmBuilderTest() throws IOException {
        var generator = new GeneratorAsciiDoc<>(FsmTest.build(), "fsm-builder");
        assertThat(generator.generateFileIfChanged(Paths.get(dir))).isFalse();
    }

    /**
     * Tests the generation of the washer finite state machine.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateGraphFsmWasherTest() throws IOException {
        var generator = new GeneratorAsciiDoc<>(FsmWasherTest.build(), "fsm-washer");
        assertThat(generator.generateFileIfChanged(Paths.get(dir))).isFalse();
    }
}
