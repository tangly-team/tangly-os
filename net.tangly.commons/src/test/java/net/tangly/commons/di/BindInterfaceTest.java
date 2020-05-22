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

import javax.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test is used to verify the behaviour of Injector dealing with interfaces.
 */
@DisplayName("Given an injector")
class BindInterfaceTest {

    interface A {
    }

    interface B {
    }

    public static class ExampleA implements A {
    }

    public static class ExampleA_B implements A, B {
    }

    public static class ExampleB implements B {
    }

    @Singleton
    interface WannabeSingleton {
    }

    public static class NonSingleton implements WannabeSingleton {
    }

    interface SuperInterface {
    }

    interface SubInterface extends SuperInterface {
    }

    static abstract class AbstractA implements A {
    }

    private Injector injector;

    @BeforeEach
    void setup() {
        injector = Injector.create();
    }

    @Nested
    @DisplayName("When injecting an interface")
    class InterfaceInjectionTest {

        @Test
        @DisplayName("Then registration the interface without explicit binding fails")
        void testImplicitBindingInterfaceFail() {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.instance(A.class));
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.instance(B.class));
        }

        @Test
        @DisplayName("Then registration with a concrete class implementing the interface succeeds")
        void testWithConcreteClassBinding() {
            injector.bind(A.class, ExampleA.class);
            assertThat(injector.instance(A.class)).isNotNull().isInstanceOf(ExampleA.class);
        }

        @Test
        @DisplayName("Then registration succeeds with a provider")
        void testAbstractClassWithProvider() {
            injector.bindProvider(A.class, ExampleA::new);
            final A instance = injector.instance(A.class);
            assertThat(instance).isNotNull().isInstanceOf(A.class);
            assertThat(instance).isNotNull().isInstanceOf(ExampleA.class);
        }

        @Test
        @DisplayName("Then the last registration of multiple registration is used to create an instance")
        void testLastBindingIsUsed() {
            injector.bind(A.class, ExampleA.class);
            injector.bind(A.class, ExampleA_B.class);
            assertThat(injector.instance(A.class)).isNotNull().isInstanceOf(ExampleA_B.class);
        }

        @Test
        @DisplayName("Then singleton on interface implies only one concrete instance will be created at most")
        void testInterfacesCantBeMarkedAsSingleton() {
            injector.bind(WannabeSingleton.class, NonSingleton.class);
            final WannabeSingleton instanceOne = injector.instance(WannabeSingleton.class);
            final WannabeSingleton instanceTwo = injector.instance(WannabeSingleton.class);
            assertThat(instanceOne).isSameAs(instanceTwo);
        }

        @Test
        @DisplayName("Then binding an interface to another interface fails")
        void testBindInterfaceToInterfaceFail() {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.bind(SuperInterface.class, SubInterface.class));
        }

        @Test
        @DisplayName("Then binding an interface to an abstract class fails")
        void testBindInterfaceToAbstractClassFail() {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> injector.bind(A.class, AbstractA.class));
        }
    }
}
