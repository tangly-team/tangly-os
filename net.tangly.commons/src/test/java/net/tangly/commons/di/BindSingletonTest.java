/*
 * Copyright 2006-2020 Marcel Baumann
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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Given an injector")
class BindSingletonTest {
    interface MyInterface {
    }

    static class Example implements MyInterface {
    }

    static abstract class AbstractExample {
    }

    private Injector injector;

    @BeforeEach
    void setup() throws Exception {
        injector = Injector.create();
    }

    @Nested
    @DisplayName("When biding a singleton")
    class SingletonInjectionTest {
        @Test
        @DisplayName("Then binding a singleton instance to a concrete class always returns the same instance")
        void testBindInstance() {
            Example example = new Example();
            injector.bindSingleton(Example.class, example);
            final Example instance = injector.instance(Example.class);
            assertThat(instance).isSameAs(example);
        }

        @Test
        @DisplayName("Then binding a singleton instance to an interface always returns the same instance")
        void testBindinterface() {
            Example example = new Example();
            injector.bindSingleton(MyInterface.class, example);
            final MyInterface instance = injector.instance(MyInterface.class);
            assertThat(instance).isSameAs(example);
        }

        @Test
        @DisplayName("Then binding a singleton instance to an abstract class always returns the same instance")
        void testBindAbstractClass() {
            AbstractExample example = new AbstractExample() {
            };
            injector.bindSingleton(AbstractExample.class, example);
            final AbstractExample instance = injector.instance(AbstractExample.class);
            assertThat(instance).isSameAs(example);
        }
    }
}
