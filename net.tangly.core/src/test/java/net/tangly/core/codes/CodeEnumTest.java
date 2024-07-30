/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core.codes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class CodeEnumTest {
    /**
     * Enumeration type extended to support the code interface.
     */
    private enum EnumCode implements Code {
        CODE_TEST_1, CODE_TEST_2, CODE_TEST_3, CODE_TEST_4, CODE_TEST_5(false);

        private final boolean enabled;

        EnumCode() {
            this(true);
        }

        EnumCode(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public int id() {
            return this.ordinal();
        }

        @Override
        public String code() {
            return this.toString();
        }

        @Override
        public boolean enabled() {
            return this.enabled;
        }
    }

    @Test
    void testCodeTest() {
        final var type = CodeType.of(EnumCode.class);
        assertThat(type.codes()).isNotEmpty();
        assertThat(type.codes()).hasSize(5);
        assertThat(type.activeCodes()).hasSize(4);
        assertThat(type.inactiveCodes()).hasSize(1);
        assertThat(type.findCode(1)).isPresent();
        assertThat(type.findCode(EnumCode.CODE_TEST_1.code())).isPresent();
    }

    @Test
    void testFindAndEquivalence() {
        final var type = CodeType.of(EnumCode.class);
        var code1 = type.findCode(EnumCode.CODE_TEST_2.id());
        var code2 = type.findCode(EnumCode.CODE_TEST_2.code());
        if (code1.isPresent() && code2.isPresent()) {
            assertThat(code1).containsSame(code2.get());
            assertThat(code1).containsSame(code2.get());
            assertThat(code1.get()).hasToString("CODE_TEST_2");
        }
    }
}
