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

import javax.inject.Inject;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Given an injector")
class AbstractClassTest {

    static class Foo {
        private AbstractExample example;

        @Inject
        Foo(AbstractExample example) {
            this.example = example;
        }
    }

    static abstract class AbstractExample {
        public abstract void does();
    }

    static class Example extends AbstractExample {
        @Inject
        public Example() {
        }

        public void does() {
        }
    }

    @Module
    interface ExampleModule {
        @Binds
        AbstractExample buildExample(Example implementation);
    }

    @Component(modules = {ExampleModule.class})
    interface Factory {
        AbstractExample example();
        Foo foo();
    }

    @Nested
    @DisplayName("When injecting an abstract class")
    class AbstractClassInjectionTest {
        @Test
        @DisplayName("Then registration the interface without explicit binding fails")
        void testAbstractClassWithoutProviderFail() {
            Factory factory;
            //      Factory factory = Dagger_AbstractClassTest_Factory.create();
            //            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.instance(AbstractExample.class));
        }

        @Test
        @DisplayName("Then registration succeeds with a provider")
        void testAbstractClassWithProvider() {
            //            final AbstractExample instance = injector.instance(AbstractExample.class);
            //            assertThat(instance).isNotNull().isInstanceOf(AbstractExample.class);
            //            assertThat(instance).isNotNull().isInstanceOf(Example.class);
        }

        @Test
        @DisplayName("Then registration succeeds with a compatible concrete subclass")
        void testAbstractClassConcreteSubclassBinding() {
            //            injector.bind(AbstractExample.class, Example.class);
            //            final AbstractExample instance = injector.instance(AbstractExample.class);
            //            assertThat(instance).isNotNull().isInstanceOf(AbstractExample.class);
            //            assertThat(instance).isNotNull().isInstanceOf(Example.class);
        }
    }
}

