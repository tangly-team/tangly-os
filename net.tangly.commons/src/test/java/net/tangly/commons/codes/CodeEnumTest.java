package net.tangly.commons.codes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class CodeEnumTest {
    private enum EnumCode implements Code {
        CODE_TEST_1, CODE_TEST_2, CODE_TEST_3, CODE_TEST_4, CODE_TEST_5;


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
            return true;
        }
    }

    @Test
    void testCodeTest() {
        final CodeType<EnumCode> type = CodeType.of(EnumCode.class);
        assertThat(type.codes()).isNotEmpty();
        assertThat(type.codes().size()).isEqualTo(5);
        assertThat(type.activeCodes().size()).isEqualTo(5);
        assertThat(type.inactiveCodes().size()).isEqualTo(0);
        assertThat(type.findCode(1).isPresent()).isTrue();
        assertThat(type.findCode(EnumCode.CODE_TEST_1.code()).isPresent()).isTrue();
    }
}
