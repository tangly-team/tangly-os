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

package net.tangly.commons.vaadin;

import net.tangly.core.BankConnection;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BankConnectionFieldTest {
    public static final String IBAN = "CH88 0900 0000 3064 1768 2";
    public static final String BIC = "POFICHBEXXX";

    @Test
    void testAddressField() {
        BankConnection connection = new BankConnection(IBAN, BIC, "");
        final BankConnectionField bankConnectionField = new BankConnectionField();
        bankConnectionField.setValue(connection);
        assertThat(bankConnectionField.getValue()).isEqualTo(connection);
    }
}
