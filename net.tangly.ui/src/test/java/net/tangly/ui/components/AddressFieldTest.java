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

package net.tangly.ui.components;

import net.tangly.core.Address;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressFieldTest {
    @Test
    void testAddressField() {
        Address address = Address.builder().postcode("6300").locality("Zug").street("Bahnhofstrasse 1").region("ZG").country("CH").build();
        final AddressField addressField = new AddressField();
        addressField.setValue(address);
        assertThat(addressField.getValue()).isEqualTo(address);
    }
}
