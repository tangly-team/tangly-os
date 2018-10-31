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

package net.tangly.fsm.imp;

import net.tangly.fsm.utilities.GeneratorGraphDot;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test set for the generation of dot descriptions of finite state machines.
 */
class GeneratorGraphDotTest {
    private final String dir = "/Users/Shared/Projects/tangly-os/net.tangly.fsm/src/site/pics";

    @Test
    void generateGraphFsmLocalTest() {
        var generator = new GeneratorGraphDot<>(FsmBbv.build(), "fsm-bbv");
        var stream = new StringWriter();
        var writer = new PrintWriter(stream);
        generator.generate(new PrintWriter(stream));
        writer.flush();
        writer.close();
        assertThat(stream.toString().length()).isGreaterThan(0);
    }

    /**
     * Tests the generation of the bbv finite state machine.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    @Tag("localTest")
    void generateGraphFsmTest() throws IOException {
        var generator = new GeneratorGraphDot<>(FsmBbv.build(), "fsm-bbv");
        assertThat(generator.generateFileIfChanged(Paths.get(dir))).isFalse();
    }

    /**
     * Tests the generation of the builder finite state machine.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    @Tag("localTest")
    void generateGraphFsmBuilderTest() throws IOException {
        var generator = new GeneratorGraphDot<>(FsmTest.build(), "fsm-test");
        assertThat(generator.generateFileIfChanged(Paths.get(dir))).isFalse();
    }

    /**
     * Tests the generation of the washer finite state machine.
     *
     * @throws FileNotFoundException if file could not be created and updated
     */
    @Test
    @Tag("localTest")
    void generateGraphFsmWasherTest() throws IOException {
        var generator = new GeneratorGraphDot<>(FsmWasherTest.build(), "fsm-washer");
        assertThat(generator.generateFileIfChanged(Paths.get(dir))).isFalse();
    }
}
