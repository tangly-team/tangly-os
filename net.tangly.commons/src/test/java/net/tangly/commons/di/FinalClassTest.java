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

@DisplayName("Given an injector")
public class FinalClassTest {

    public final class Dependency {
        public Dependency() {
        }
    }

    public final class Example {
        Dependency dep;

        public Example(Dependency dep) {
            this.dep = dep;
        }
    }

    private Injector injector;

    @BeforeEach
    void setUp() throws Exception {
        injector = Injector.create();
    }

    @Nested
    @DisplayName("When binding a final class")
    class FinalClassInjectionTest {
        @Test
        @DisplayName("Then final class can be instantiated and depency through constructor parameter is instantiated and set")
        void success_finalClassCanBeInstantiated() {
            final Example instance = injector.instance(Example.class);
            assertThat(instance).isNotNull();
            assertThat(instance.dep).isNotNull();
        }
    }
}
