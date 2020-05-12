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

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Given an injector")
class BindProviderTest {

    public static class ThirdParty {
        public ThirdParty() {
        }

        static ThirdParty thirdPartyFactory() {
            return new ThirdParty();
        }
    }

    interface MyInterface {
    }

    @Singleton
    private static class MySingleton {
    }

    public static class Example {
        final ThirdParty dep;

        public Example(ThirdParty dep) {
            this.dep = dep;
        }
    }

    private Injector injector;

    @BeforeEach
    void setUp() throws Exception {
        injector = Injector.create();
    }

    @Nested
    @DisplayName("When injecting a class with a provider")
    class ProviderInjectionTest {
        @Test
        @DisplayName("Then an instance is created with constructor provider")
        void testProviderNewIsAvailable() {
            injector.bindProvider(ThirdParty.class, (Provider<ThirdParty>) ThirdParty::new);
            final Example instance = injector.instance(Example.class);
            assertThat(instance).isNotNull();
            assertThat(instance.dep).isNotNull();
        }

        @Test
        @DisplayName("Then an instance is created with method provider")
        void testProviderMethodIsAvailable() {
            injector.bindProvider(ThirdParty.class, (Provider<ThirdParty>) ThirdParty::thirdPartyFactory);
            final Example instance = injector.instance(Example.class);
            assertThat(instance).isNotNull();
            assertThat(instance.dep).isNotNull();
        }

        @Test
        @DisplayName("Then an instance is created through a lambda expression defining a concrete anonymous class of the interface")
        void testProviderForInterface() {
            injector.bindProvider(MyInterface.class, () -> new MyInterface() {
            });
            final MyInterface instance = injector.instance(MyInterface.class);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("Then a class marked as singleton is only constructed at most once with the associated provider")
        void testProviderAndSingleton() {
            AtomicInteger counter = new AtomicInteger(0);
            injector.bindProvider(MySingleton.class, () -> {
                counter.incrementAndGet();
                return new MySingleton();
            });
            final MySingleton firstInstance = injector.instance(MySingleton.class);
            final MySingleton secondInstance = injector.instance(MySingleton.class);
            assertThat(firstInstance).isSameAs(secondInstance);
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Then fails if the provider is throwing an exception")
        void testProviderThrowsExceptionFail() {
            injector.bindProvider(ThirdParty.class, () -> {
                throw new NullPointerException("Too bad :-(");
            });
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> injector.instance(ThirdParty.class));
        }
    }
}
