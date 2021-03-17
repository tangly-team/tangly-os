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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

/**
 * Represents a use case in a {@link Story}. The annotation is meta-annotated with JUnit 5 Jupiter's built-in "@Test" annotation. When IDEs and test
 * engines scan through a given set of test classes and find this custom @Scenario annotation on public instance methods, they mark those methods as
 * test methods to be executed.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Test
public @interface Scenario {
    /**
     * Returns the description of the scenario as human readable information for living documentation.
     *
     * @return representation of the use case in a plain human language.
     */
    String value();
}
