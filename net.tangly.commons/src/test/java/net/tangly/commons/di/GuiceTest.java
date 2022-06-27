/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.di;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Given an injector")
class GuiceTest {
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

    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(AbstractExample.class).to(Example.class);
        }
    }

    @Nested
    @DisplayName("When injecting an abstract class")
    class AbstractClassInjectionTest {
        @Test
        @DisplayName("Then registration the interface without explicit binding fails")
        void testAbstractClassWithoutProviderFail() {
            Injector injector = Guice.createInjector();
            assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> injector.getInstance(AbstractExample.class));
        }

        @Test
        @DisplayName("Then registration succeeds with a compatible concrete subclass")
        void testAbstractClassConcreteSubclassBinding() {
            Injector injector = Guice.createInjector(new TestModule());
            AbstractExample instance = injector.getInstance(AbstractExample.class);
            assertThat(instance).isNotNull().isInstanceOf(AbstractExample.class).isInstanceOf(Example.class);
        }
    }
}
