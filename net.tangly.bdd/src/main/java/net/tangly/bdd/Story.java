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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.tangly.bdd.engine.StoryExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;

/**
 * Represents a story in BDD or specification by example style of writing tests. Notice that the annotation is meta-annotated with JUnit 5 built-in
 * "@Testable" annotation. This annotation gives IDEs and other tools a way to identify classes and methods that are testable––meaning the annotated
 * class or method can be executed by a test engine like JUnit 5 Jupiter test engine.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Testable
@ExtendWith(StoryExtension.class)
public @interface Story {
    /**
     * Returns the name of the story as human readable information for living documentation.
     *
     * @return short summary of the story in a plain human language.
     */
    String value();

    /**
     * Returns the detailed description of the story as human readable information for living documentation. One could describe a story in this
     * format: As a {user-defined string}, in order to {user-defined string}, I want to {user-defined string}.
     *
     * @return the story in a plain human language.
     */
    String description() default "";

    /**
     * Returns the identifier of the story. The identifier can be used for cross-reference and traceability of the requirements.
     *
     * @return identifier of the story unique in the context of the application domain
     */
    String id() default "";

    /**
     * Returns the tags associated with the feature. Tags provide domain specific classification and additional information.
     *
     * @return tags defined in the feature
     */
    String[] tags() default {};

}
