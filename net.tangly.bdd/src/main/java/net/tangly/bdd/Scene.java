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

package net.tangly.bdd;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/**
 * A scene is central to BDD or specification by example style of writing tests. It describes a use case in a story or specification and holds all the
 * information required to execute a specific scenario. The Scene class enables test writers to define scenarios (behaviors) using steps like "given",
 * "then", and "when" that are written as lambda expressions. The Scene class is the central unit of our custom extension that holds test method
 * specific state information. The state information can be passed around between the various steps of a scenario. We use the “BeforeEachCallback”
 * interface to prepare a Scene instance right before the invocation of a test method.
 */
public class Scene {
    private final Method method;
    private final Map<String, Object> values;
    private Phase current;
    private Phase given;
    private Phase when;
    private Phase then;

    public Scene(@NotNull Method method) {
        this.method = method;
        this.values = new HashMap<>();
    }

    public void put(String key, Object value) {
        values.put(key, value);
    }

    public Object get(String key) {
        return values.get(key);
    }

    public String methodName() {
        return method.getName();
    }

    public String description() {
        return method.getAnnotation(Scenario.class).value();
    }

    public Scene given(String description, Consumer<Scene> given) {
        if ((this.given != null) || (this.when != null) || (this.then != null)) {
            throw new IllegalStateException("Cannot define more than one \"given\" phase or after a \"when\" or \"then\" phase.");
        }
        this.given = new Phase(description, given);
        current = this.given;
        return this;
    }

    public Scene when(String description, Consumer<Scene> when) {
        if ((this.when != null) || (this.then != null)) {
            throw new IllegalStateException("Cannot define more than one \"when\" phase or after a \"then\" phase.");
        }
        this.when = new Phase(description, when);
        current = this.when;
        return this;
    }

    public Scene then(String description, Consumer<Scene> then) {
        if (this.then != null) {
            throw new IllegalStateException("Cannot define more than one \"then\" phase.");
        }
        this.then = new Phase(description, then);
        current = this.then;
        return this;
    }

    public Scene and(String description, Consumer<Scene> and) {
        if (current == null) {
            throw new IllegalStateException("Start your scene with \"given\" or \"when\" pr \"then\" phase.");
        }
        current.and(description, and);
        return this;
    }

    public Scene run() {
        Objects.requireNonNull(given, "Start your scene with a \"given\" phase");
        Objects.requireNonNull(when, "Start your scene with a \"when\" phase");
        Objects.requireNonNull(then, "End your scene with a \"then\" phase");
        given.lambda().accept(this);
        when.lambda().accept(this);
        then.lambda().accept(this);
        return this;
    }

    public Phase given() {
        return given;
    }

    public Phase when() {
        return when;
    }

    public Phase then() {
        return then;
    }
}
