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

package net.tangly.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailAddressTest {
     static final String CORRECT_ADDRESS = "john.doe@somewhere.edu";
     static final String WRONG_ADDRESS_1 = "john.doe-somewhere.edu";
     static final String WRONG_ADDRESS_2 = "                      ";
     static final String WRONG_ADDRESS_3 = "@somewhere.edu";

     @Test
     void testConstructors() {
         assertThat(new EmailAddress("john.doe", "somewhere.edu")).isNotNull();
         assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new EmailAddress("", ""));
         assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new EmailAddress("", "somewhere.edu"));
         assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new EmailAddress("john.doe", ""));
     }

    @Test
    void testAddressOf() {
        assertThat(EmailAddress.of(CORRECT_ADDRESS)).isNotNull();
        assertThat(EmailAddress.of(WRONG_ADDRESS_1)).isNull();
        assertThat(EmailAddress.of(WRONG_ADDRESS_2)).isNull();
        assertThat(EmailAddress.of(WRONG_ADDRESS_3)).isNull();
    }

    @Test
    void testValidation() {
        assertThat(EmailAddress.isValid(CORRECT_ADDRESS)).isTrue();
        assertThat(EmailAddress.isValid(WRONG_ADDRESS_1)).isFalse();
        assertThat(EmailAddress.isValid(WRONG_ADDRESS_2)).isFalse();
        assertThat(EmailAddress.isValid(WRONG_ADDRESS_3)).isFalse();
    }

    @Test
    void testToString() {
        var address = EmailAddress.of(CORRECT_ADDRESS);
        assertThat(address.text()).isEqualTo(CORRECT_ADDRESS);
        assertThat(EmailAddress.of(address.text())).isEqualTo(address);
    }
}
