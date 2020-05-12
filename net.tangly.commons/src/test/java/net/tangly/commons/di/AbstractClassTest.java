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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


@DisplayName("Given an injector")
class AbstractClassTest {
    public static abstract class AbstractExample {
    }

    public static class Example extends AbstractExample {
    }

    private Injector injector;

    @BeforeEach
    void setUp() throws Exception {
        injector = Injector.create();
    }

    @Nested
    @DisplayName("When injecting an abstract class")
    class AbstractClassInjectionTest {
        @Test
        @DisplayName("Then registration the interface without explicit binding fails")
        void testAbstractClassWithoutProviderFail() {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.instance(AbstractExample.class));
        }

        @Test
        @DisplayName("Then registration succeeds with a provider")
        void testAbstractClassWithProvider() {
            injector.bindProvider(AbstractExample.class, Example::new);
            final AbstractExample instance = injector.instance(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(Example.class);
        }

        @Test
        @DisplayName("Then registration succeeds with a compatible concrete subclass")
        void testAbstractClassConcreteSubclassBinding() {
            injector.bind(AbstractExample.class, Example.class);
            final AbstractExample instance = injector.instance(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(Example.class);
        }
    }

    @Nested
    @DisplayName("When injecting an abstract class through a module")
    class AbstractClassModuleInjectTest {
        @Test
        @DisplayName("Then registration the interface without explicit binding fails")
        void testAbstractClassWithoutProviderFail() {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Injector.create((o) -> o.bind(AbstractExample.class)));
        }

        @Test
        @DisplayName("Then registration succeeds with a provider")
        void testAbstractClassWithProvider() {
            Injector injector = Injector.create((o) -> o.bindProvider(AbstractExample.class, Example::new));
            final AbstractExample instance = injector.instance(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(Example.class);
        }

        @Test
        @DisplayName("Then registration succeeds with a compatible concrete subclass")
        void testAbstractClassConcreteSubclassBinding() {
            Injector injector = Injector.create((o) -> o.bind(AbstractExample.class, Example.class));
            final AbstractExample instance = injector.instance(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(Example.class);
        }
    }
}
