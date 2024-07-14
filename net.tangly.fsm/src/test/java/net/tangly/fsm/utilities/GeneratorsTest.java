/*
 * Copyright 2006-2024 Marcel Baumann
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

import net.tangly.fsm.imp.FsmBbv;
import net.tangly.fsm.imp.FsmTest;
import net.tangly.fsm.imp.FsmWasherTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test set for various format descriptions of finite state machines. A support function generates all the descriptions in the project directory for documentation purposes.
 */
class GeneratorsTest {
    private static final String BBV = "fsm-bbv";
    private static final String TEST = "fsm-test";
    private static final String WASHER = "fsm-washer";

    /**
     * Create for each defined generator the statechart for the three finite state machines defined for testing. All files are written into a project-specific folder referenced
     * from the project documentation and static website generation.
     *
     * @param args arguments of the program
     * @throws IOException if a file could not be opened or written
     */
    public static void main(String[] args) throws IOException {
        createFiles();
    }

    @BeforeAll
    static void createFiles() throws IOException {
        createStatechart(new GeneratorPlantUml<>(FsmBbv.build(), BBV));
        createStatechart(new GeneratorPlantUml<>(FsmTest.build(), TEST));
        createStatechart(new GeneratorPlantUml<>(FsmWasherTest.build(), WASHER));

        createStatechart(new GeneratorMermaid<>(FsmBbv.build(), BBV));
        createStatechart(new GeneratorMermaid<>(FsmTest.build(), TEST));
        createStatechart(new GeneratorMermaid<>(FsmWasherTest.build(), WASHER));

        createStatechart(new GeneratorGraphDot<>(FsmBbv.build(), BBV));
        createStatechart(new GeneratorGraphDot<>(FsmTest.build(), TEST));
        createStatechart(new GeneratorGraphDot<>(FsmWasherTest.build(), WASHER));

        createStatechart(new GeneratorStateMachineCat<>(FsmBbv.build(), BBV));
        createStatechart(new GeneratorStateMachineCat<>(FsmTest.build(), TEST));
        createStatechart(new GeneratorStateMachineCat<>(FsmWasherTest.build(), WASHER));

        createStatechart(new GeneratorAsciiDoc<>(FsmBbv.build(), BBV));
        createStatechart(new GeneratorAsciiDoc<>(FsmTest.build(), TEST));
        createStatechart(new GeneratorAsciiDoc<>(FsmWasherTest.build(), WASHER));
    }

    private static <O, S extends Enum<S>, E extends Enum<E>> void createStatechart(Generator<O, S, E> generator) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path().resolve(generator.name() + "." + generator.extension()),
            StandardCharsets.UTF_8))) {
            generator.generate(writer);
        }
    }

    @Test
    void generateGraphLocalTest() {
        generateLocalTest(new GeneratorPlantUml<>(FsmTest.build(), TEST));
        generateLocalTest(new GeneratorMermaid<>(FsmTest.build(), TEST));
        generateLocalTest(new GeneratorGraphDot<>(FsmTest.build(), TEST));
        generateLocalTest(new GeneratorAsciiDoc<>(FsmTest.build(), TEST));
    }

    /**
     * Tests the generation of plantUML FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generatePlantMermaidTest() throws IOException {
        generateTest(new GeneratorMermaid<>(FsmBbv.build(), BBV));
        generateTest(new GeneratorMermaid<>(FsmTest.build(), TEST));
        generateTest(new GeneratorMermaid<>(FsmWasherTest.build(), WASHER));
    }

    /**
     * Tests the generation of plantUML FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generatePlantUmlTest() throws IOException {
        generateTest(new GeneratorPlantUml<>(FsmBbv.build(), BBV));
        generateTest(new GeneratorPlantUml<>(FsmTest.build(), TEST));
        generateTest(new GeneratorPlantUml<>(FsmWasherTest.build(), WASHER));
    }

    /**
     * Tests the generation of dot FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateDotTest() throws IOException {
        generateTest(new GeneratorGraphDot<>(FsmBbv.build(), BBV));
        generateTest(new GeneratorGraphDot<>(FsmTest.build(), TEST));
        generateTest(new GeneratorGraphDot<>(FsmWasherTest.build(), WASHER));
    }

    /**
     * Tests the generation of state machine cat FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateStateMachineCatTest() throws IOException {
        generateTest(new GeneratorStateMachineCat<>(FsmBbv.build(), BBV));
        generateTest(new GeneratorStateMachineCat<>(FsmTest.build(), TEST));
        generateTest(new GeneratorStateMachineCat<>(FsmWasherTest.build(), WASHER));
    }

    /**
     * Tests the generation of asciidoc FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateAsciiDocTest() throws IOException {
        generateTest(new GeneratorAsciiDoc<>(FsmBbv.build(), BBV));
        generateTest(new GeneratorAsciiDoc<>(FsmTest.build(), TEST));
        generateTest(new GeneratorAsciiDoc<>(FsmWasherTest.build(), WASHER));
    }

    /**
     * @param generator generator used to create the representation
     * @param <O>       Owning class of the finite state machine owner
     * @param <S>       State enum class
     * @param <E>       Event enum class
     * @throws IOException if the file could not be read or written
     */
    private <O, S extends Enum<S>, E extends Enum<E>> void generateTest(Generator<O, S, E> generator) throws IOException {
        assertThat(generator.generateFileIfChanged(path())).isFalse();
    }

    private <O, S extends Enum<S>, E extends Enum<E>> void generateLocalTest(Generator<O, S, E> generator) {
        var stream = new StringWriter();
        var writer = new PrintWriter(stream);
        generator.generate(new PrintWriter(stream));
        writer.flush();
        writer.close();
        assertThat(stream.toString()).isNotEmpty();
    }

    private <O, S extends Enum<S>, E extends Enum<E>> void createDescription(Generator<O, S, E> generator) {
        var stream = new StringWriter();
        var writer = new PrintWriter(stream);
        generator.generate(new PrintWriter(stream));
        writer.flush();
        writer.close();
        assertThat(stream.toString()).isNotEmpty();
    }

    static private Path path() {
        URL resource = GeneratorsTest.class.getClassLoader().getResource("files/");
        try {
            return Paths.get(resource.toURI()).toFile().toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
