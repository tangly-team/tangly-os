/*
 * Copyright 2006-2019 Marcel Baumann
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

package net.tangly.fsm.imp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.tangly.fsm.utilities.Generator;
import net.tangly.fsm.utilities.GeneratorAsciiDoc;
import net.tangly.fsm.utilities.GeneratorGraphDot;
import net.tangly.fsm.utilities.GeneratorPlantUml;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test set for the generation of various format descriptions of finite state machines.
 */
class GeneratorsTest {
    @Test
    void generateGraphLocalTest() {
        generateLocalTest(new GeneratorPlantUml<>(FsmBbv.build(), "fsm-bbv"));
        generateLocalTest(new GeneratorGraphDot<>(FsmTest.build(), "fsm-test"));
        generateLocalTest(new GeneratorAsciiDoc<>(FsmWasherTest.build(), "fsm-washer"));
    }

    /**
     * Tests the generation of plant UML FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generatePlantUmlTest() throws IOException {
        generateTest(new GeneratorPlantUml<>(FsmBbv.build(), "fsm-bbv"));
        generateTest(new GeneratorPlantUml<>(FsmTest.build(), "fsm-test"));
        generateTest(new GeneratorPlantUml<>(FsmWasherTest.build(), "fsm-washer"));
    }

    /**
     * Tests the generation of dot FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateDotTest() throws IOException {
        generateTest(new GeneratorGraphDot<>(FsmBbv.build(), "fsm-bbv"));
        generateTest(new GeneratorGraphDot<>(FsmTest.build(), "fsm-test"));
        generateTest(new GeneratorGraphDot<>(FsmWasherTest.build(), "fsm-washer"));
    }

    /**
     * Tests the generation of asciidoc FSM representation of the bbv, test and washer finite state machines.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    void generateAsciiDocTest() throws IOException {
        generateTest(new GeneratorAsciiDoc<>(FsmBbv.build(), "fsm-bbv"));
        generateTest(new GeneratorAsciiDoc<>(FsmTest.build(), "fsm-test"));
        generateTest(new GeneratorAsciiDoc<>(FsmWasherTest.build(), "fsm-washer"));
    }

    /**
     * @param generator generator used to create the representation
     * @param <O>       Owning class of the finite state machine owner
     * @param <S>       State enum class
     * @param <E>       Event enum class
     * @throws IOException if the file could not be read or written
     * @implNote if it is necessary to generate the reference files replace path() with Paths.get("/Users/Shared/Projects/tangly-os/net.tangly
     * .fsm/src/test/resources/files")
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
        assertThat(stream.toString().length()).isGreaterThan(0);
    }

    private Path path() {
        URL resource = getClass().getClassLoader().getResource("files/");
        try {
            return Paths.get(resource.toURI()).toFile().toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
