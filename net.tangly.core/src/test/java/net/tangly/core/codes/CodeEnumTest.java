/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.codes;

import java.util.Optional;

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
        public boolean isEnabled() {
            return this.enabled;
        }
    }

    @Test
    void testCodeTest() {
        final CodeType<EnumCode> type = CodeType.of(EnumCode.class);
        assertThat(type.codes()).isNotEmpty();
        assertThat(type.codes().size()).isEqualTo(5);
        assertThat(type.activeCodes().size()).isEqualTo(4);
        assertThat(type.inactiveCodes().size()).isEqualTo(1);
        assertThat(type.findCode(1).isPresent()).isTrue();
        assertThat(type.findCode(EnumCode.CODE_TEST_1.code()).isPresent()).isTrue();
    }

    @Test
    void testFindAndEquivalence() {
        final CodeType<EnumCode> type = CodeType.of(EnumCode.class);
        Optional<EnumCode> code1 = type.findCode(EnumCode.CODE_TEST_2.id());
        Optional<EnumCode> code2 = type.findCode(EnumCode.CODE_TEST_2.code());
        if (code1.isPresent() && code2.isPresent()) {
            assertThat(code1.get() == code2.get()).isTrue();
            assertThat(code1.get().equals(code2.get())).isTrue();
            assertThat(code1.get().toString()).isEqualTo("CODE_TEST_2");
        }
    }
}
