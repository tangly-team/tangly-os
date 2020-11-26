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

package net.tangly.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PhoneNrTest {
    public static final String PHONE_NR = "+41 79 778 86 89";

    @Test
    void testOf() {
        PhoneNr phoneNr = PhoneNr.of(PHONE_NR);
        assertThat(phoneNr.isValid()).isTrue();
        assertThat(phoneNr.number()).isEqualTo(PHONE_NR);
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
