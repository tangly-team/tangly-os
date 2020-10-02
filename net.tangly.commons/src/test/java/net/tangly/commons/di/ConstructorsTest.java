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

package net.tangly.commons.di;


import java.util.concurrent.CompletionException;
import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Given an injector")
public class ConstructorsTest {
    public static class ExampleOne {
        public ExampleOne() {
        }
    }

    public static class ExampleTwo {
        public ExampleTwo() {
        }

        public ExampleTwo(String helloworld) {
        }
    }

    public static class ExampleThree {
        public ExampleThree() {
        }

        ExampleThree(String helloWorld) {
        }
    }

    static class ExampleFour {
        @Inject
        public ExampleFour() {
        }

        @Inject
        public ExampleFour(String helloworld) {
        }
    }


    public static class ExampleFive {
        @Inject
        public ExampleFive() {
        }

        public ExampleFive(String helloWorld) {
        }
    }

    static class ExampleSix {
        private ExampleSix() {
        }
    }

    static class ExampleSeven {
        public ExampleSeven() {
            throw new NullPointerException();
        }
    }

    static class ExampleEight {
        public ExampleEight(ExampleEight example) {
        }
    }

    public static class Dependency {
        public Dependency() {
        }
    }

    public static class ExampleNine {
        private final Dependency dependency;

        public ExampleNine(Dependency dependency) {
            this.dependency = dependency;
        }
    }

    @Nested
    @DisplayName("When injecting a constructor")
    class ConstructorInjectionTest {
        @Test
        @DisplayName("Then binding a class with a simple public no arguments constructor allows instantiation")
        public void testBindPublicNoArgConstructor() {
//            final ExampleNine instance = injector.instance(ExampleNine.class);
//            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("Then binding a class with multiple non-annotated constructors fails")
        void testMultipleConstructorsNotAnnotatedFail() {
//            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.instance(ExampleTwo.class));
        }

        @Test
        @DisplayName("Then binding a class with multiple annotated constructors fails")
        void testMultipleConstructorsTwoWithAnnotationFail() {
//            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.instance(ExampleFour.class));
        }

        @Test
        @DisplayName("Then binding a class with multiple constructors and exactly one annotated allows instantiation")
        void testMultipleConstructorsOneWithAnnotation() {
//            final ExampleFive instance = injector.instance(ExampleFive.class);
//            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("Then binding a class with multiple constructors and exactly one public allows instantiation")
        void testMultipleConstructorsOnlyOnePublic() {
//            final ExampleThree instance = injector.instance(ExampleThree.class);
//            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("Then binding a class with a single private constructor fails")
        void testOnlyPrivateDeclaredConstructorFail() {
//            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.instance(ExampleSix.class));
        }

        @Test
        @DisplayName("Then binding a class with a constructor with one argument allows instantiation of instance and constructor parameter")
        void testConstructorWithOneDependency() {
//            final ExampleNine instance = injector.instance(ExampleNine.class);
//            assertThat(instance).isNotNull();
//            assertThat(instance.dependency).isNotNull();
        }

        @Test
        @DisplayName("Then binding a class with cyclic dependency fails")
        void testRecursiveConstructorArgumentsFail() {
//            assertThatExceptionOfType(StackOverflowError.class).isThrownBy(() -> injector.instance(ExampleEight.class));
        }

        @Test
        @DisplayName("Then binding a class which constructor throws an exception fails at instantiation")
        void testExceptionInConstructorFail() {
//            assertThatExceptionOfType(CompletionException.class).isThrownBy(() -> injector.instance(ExampleSeven.class));
        }
    }
}
