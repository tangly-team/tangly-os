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

class BankConnectionTest {
    public static final String IBAN = "CH88 0900 0000 3064 1768 2";
    public static final String BIC = "POFICHBEXXX";
    static final String CONNECTION = "CH88 0900 0000 3064 1768 2,POFICHBEXXX,Postfinanz Schweiz";
    static final String CONNECTION_WITH_SPACES = "CH88 0900 0000 3064 1768 2, POFICHBEXXX, Postfinanz Schweiz";

    @Test
    void testConstructors() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new BankConnection(null, null, null)).withMessageContaining("Illegal IBAN");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new BankConnection(null)).withMessageContaining("Illegal IBAN");
        assertThat(new BankConnection(IBAN, null, null)).isNotNull();
        assertThat(new BankConnection(IBAN)).isNotNull();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new BankConnection(IBAN, "foo", null)).withMessageContaining("Illegal BIC");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new BankConnection(IBAN, "foo")).withMessageContaining("Illegal BIC");
        assertThat(new BankConnection(IBAN, BIC, null)).isNotNull();
        assertThat(new BankConnection(IBAN, BIC)).isNotNull();
    }

    @Test
    void testOf() {
        assertThat(BankConnection.of(null, null, null)).isNull();
        assertThat(BankConnection.of(IBAN, null, null)).isNotNull();
        assertThat(BankConnection.of(IBAN, "wrong bic", null)).isNull();
        assertThat(BankConnection.of(IBAN, BIC, null)).isNotNull();
    }

    @Test
    void testToText() {
        var connection = BankConnection.of(CONNECTION);
        assertThat(connection.text()).isEqualTo(CONNECTION);

        connection = BankConnection.of(CONNECTION_WITH_SPACES);
        assertThat(connection.text()).isEqualTo(CONNECTION);

        connection = new BankConnection(IBAN, BIC, "Postfinanz Schweiz");
        assertThat(connection.text()).isEqualTo(connection.text());
        assertThat(BankConnection.of(connection.text())).isEqualTo(connection);

        connection = new BankConnection(IBAN, BIC);
        assertThat(BankConnection.of(connection.text())).isEqualTo(connection);

        connection = new BankConnection(IBAN);
        assertThat(BankConnection.of(connection.text())).isEqualTo(connection);
    }

    @Test
    void validateIbanAndBicTest() {
        assertThat(BankConnection.validateIban(IBAN)).isTrue();
        assertThat(BankConnection.validateIban("DE27 1007 7777 0209 2997 00")).isTrue();
        assertThat(BankConnection.validateIban("BE68 8440 1037 0034")).isTrue();
        assertThat(BankConnection.validateIban("FR76 3006 6100 4100 0105 7380 116")).isTrue();
        assertThat(BankConnection.validateIban("IT68 D030 0203 2800 0040 0162 854")).isTrue();
        assertThat(BankConnection.validateIban("LI10 0880 0000 0201 7630 6")).isTrue();
        assertThat(BankConnection.validateIban("PT50 0035 0683 0000 0007 8431 1")).isTrue();
        assertThat(BankConnection.validateIban("AT02 2050 3021 0102 3600")).isTrue();
        assertThat(BankConnection.validateBic(BIC)).isTrue();
        assertThat(BankConnection.validateBic("UBSBCHZZXXX")).isTrue();
    }

    @Test
    void validateInvalidIbanAndInvalidBicTest() {
        assertThat(BankConnection.validateIban("CH8809000000306417682")).isFalse();
        assertThat(BankConnection.validateBic("DUMMYBIC")).isFalse();
    }
}
