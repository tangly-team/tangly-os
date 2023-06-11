/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PhoneNrTest {
    public static final String PHONE_NR = "+41 79 778 86 89";
    public static final String ILLEGAL_PHONE_NR = "+41 XX &A7B";

    @Test
    void testOf() {
        assertThat(PhoneNr.of(PHONE_NR)).isNotNull();
        assertThat(PhoneNr.of(ILLEGAL_PHONE_NR)).isNull();
        assertThat(PhoneNr.isValid(PHONE_NR)).isTrue();
        assertThat(PhoneNr.of(PHONE_NR).number()).isEqualTo(PHONE_NR);
    }

    @Test
    void testConstructors() {
        assertThat(new PhoneNr(PHONE_NR)).isNotNull();
        assertThat(new PhoneNr(PHONE_NR).number()).isEqualTo(PHONE_NR);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new PhoneNr(ILLEGAL_PHONE_NR)).withMessageContaining("Illegal phone number");
    }

    @Test
    void testFormat() {
        assertThat(PhoneNr.of("+41797788689").number()).isEqualTo(PHONE_NR);
        assertThat(PhoneNr.of("+41 (0)797788689").number()).isEqualTo(PHONE_NR);
        assertThat(PhoneNr.of("+41-79-778-86-89").number()).isEqualTo(PHONE_NR);
        assertThat(PhoneNr.of("+41-(0)79-778-86-89").number()).isEqualTo(PHONE_NR);
        assertThat(PhoneNr.of("++41797788689").number()).isEqualTo(PHONE_NR);
        assertThat(PhoneNr.of("++41-79-778-86-89").number()).isEqualTo(PHONE_NR);
        assertThat(PhoneNr.of("++41-(0)79-778-86-89").number()).isEqualTo(PHONE_NR);
    }

    @Test
    void testIllegalFormat() {
        assertThat(PhoneNr.of("DUMMY")).isNull();
    }
}
