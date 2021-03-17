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

package net.tangly.bdd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a phase in a scene such as a given, when or then phase for the acceptance test modeled within the scene. Optionally, a phase holds
 * additional "and" conditions for more complex scenarios.
 */
public class Phase {
    /**
     * Human readable description of the phase. The text is used to create a living documentation of the stories.
     */
    private final String text;

    /**
     * Code fragment associated with the phase. The lambda fragments are daisy chained to emulate and connections.
     */
    private Consumer<Scene> lambda;

    /**
     * The list of phases daisy chained as and conditions. The aggregation of text descriptions is used in the living documentation.
     */
    private final List<String> ands;

    public Phase(@NotNull String text, @NotNull Consumer<Scene> lambda) {
        this.text = text;
        this.lambda = lambda;
        this.ands = new ArrayList<>();
    }

    public Consumer<Scene> lambda() {
        return lambda;
    }

    public String text() {
        return text;
    }

    public void and(String text, @NotNull Consumer<Scene> lambda) {
        this.lambda = this.lambda.andThen(lambda);
        ands.add(text);
    }

    public List<String> ands() {
        return Collections.unmodifiableList(ands);
    }
}

