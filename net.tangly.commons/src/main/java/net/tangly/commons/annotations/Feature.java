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

package net.tangly.commons.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
/**
 * A feature or a story describes a behavior of the build system. You can use a story template or free text to describe the behavior of the system.
 * For each feature a set of scenarios or tests are defined in the form "Given a context When a specific event happens then the expected outcomes are
 * as described". The "Given When Then" scenarios are described in unit tests using the features of JUnit 5 such as test class and nested classes to
 * document and implement the "Given When" and test methods to realize the "Then" part. The display name annotation is used to produce readable and
 * executable behavior driven tests.
 **/ public @interface Feature {
    String value();

    String[] tags() default {};
}