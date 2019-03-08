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

package net.tangly.commons.di;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.inject.Singleton;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Given an injector")
public class SingletonTest {

    public interface MyInterface {
    }

    @Singleton
    public static class Dependency {
        public Dependency() {
        }
    }

    public static class Other {
        private final Dependency dep;

        public Other(Dependency dep) {
            this.dep = dep;
        }
    }

    public static class ExampleZero {
        private final Dependency dep;
        private final Other other;

        public ExampleZero(Dependency dep, Other other) {
            this.dep = dep;
            this.other = other;
        }
    }

    @Singleton
    public static class ExampleOne {
        public ExampleOne() {
        }
    }

    public static class ExampleTwo {
        public ExampleTwo() {
        }
    }

    public static class DependencyThree {
        public DependencyThree() {
        }
    }

    public static class OtherThree {
        private final DependencyThree dep;

        public OtherThree(DependencyThree dep) {
            this.dep = dep;
        }
    }

    public static class ExampleThree {
        private final DependencyThree dep;
        private final OtherThree other;

        public ExampleThree(DependencyThree dep, OtherThree other) {
            this.dep = dep;
            this.other = other;
        }
    }

    private Injector injector;

    @BeforeEach
    void setup() {
        injector = Injector.create();
    }

    @Nested
    @DisplayName("When injecting a non singleton class")
    public class NonSingletonTest {

        @Test
        @DisplayName("then non singleton class instantiates new instances each time")
        void success_nonSingleton_newInstanceEverytime() {
            final ExampleTwo instanceOne = injector.instance(ExampleTwo.class);
            final ExampleTwo instanceTwo = injector.instance(ExampleTwo.class);
            assertThat(instanceOne).isNotNull().isNotSameAs(instanceTwo);
        }


        @Test
        @DisplayName("then non singleton class instantiates new instances each time, also with a bigger example")
        void success_nonSingleton_biggerExample() {
            final ExampleThree instance = injector.instance(ExampleThree.class);

            assertThat(instance.dep).isNotSameAs(instance.other.dep);
        }
    }

    @Nested
    @DisplayName("When injecting a singleton class with the @singleton annotation")
    public class AtSingletonAnnotationTest {
        @Test
        @DisplayName("Ten singleton class instantiates at most one instance")
        void success_singleton() {
            final ExampleOne instanceOne = injector.instance(ExampleOne.class);
            final ExampleOne instanceTwo = injector.instance(ExampleOne.class);
            assertThat(instanceOne).isSameAs(instanceTwo);
        }

        @Test
        @DisplayName("Then singleton class instantiates at most one instance, also with a bigger example")
        void success_singleton_biggerExample() {
            final Dependency dependency = injector.instance(Dependency.class);
            final ExampleZero instance = injector.instance(ExampleZero.class);
            assertThat(dependency).isSameAs(instance.dep).isSameAs(instance.other.dep);
        }
    }

    @Nested
    @DisplayName("When injecting a singleton class with the appropriate bind method")
    public class MarkAsSingletonTest {
        public class Example {
            public Example() {
            }
        }

        @Test
        @DisplayName("Then singleton class instantiates at most one instance")
        void testSingletonNoAnnotationButMarked() {
            injector.bindSingleton(Example.class);
            final Example instanceOne = injector.instance(Example.class);
            final Example instanceTwo = injector.instance(Example.class);
            assertThat(instanceOne).isNotNull().isSameAs(instanceTwo);
        }

        @Test
        @DisplayName("Then injecting a singleton interface fails")
        void testSingletonNoAnnotationButMarkedAndParamIsAnInterface() {
            assertThrows(IllegalArgumentException.class, () -> injector.bindSingleton(BindProviderTest.MyInterface.class));
        }
    }
}
