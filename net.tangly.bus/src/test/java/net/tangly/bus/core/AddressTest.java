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

package net.tangly.bus.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest {
    static final String STREET = "STREET";
    static final String EXTENDED = "EXTENDED";
    public static final String POBOX = "POBOX";
    public static final String POSTCODE = "POSTCODE";
    public static final String LOCALITY = "LOCALITY";
    public static final String REGION = "REGION";
    public static final String COUNTRY = "COUNTRY";

    @Test
    void buildAddressTest() {
        Address address =
                Address.builder().street(STREET).extended(EXTENDED).poBox(POBOX).locality(LOCALITY).postcode(POSTCODE).region(REGION).country(COUNTRY)
                        .build();
        assertThat(address.street()).isEqualTo(STREET);
        assertThat(address.extended()).isEqualTo(EXTENDED);
        assertThat(address.poBox()).isEqualTo(POBOX);
        assertThat(address.locality()).isEqualTo(LOCALITY);
        assertThat(address.region()).isEqualTo(REGION);
        assertThat(address.country()).isEqualTo(COUNTRY);
    }

    @Test
    void buildAddressFromString() {
        Address original =
                Address.builder().street(STREET).extended(EXTENDED).poBox(POBOX).locality(LOCALITY).postcode(POSTCODE).region(REGION).country(COUNTRY)
                        .build();
        Address copy = Address.of(original.text());
        assertThat(copy).isEqualTo(original);
        assertThat(copy.hashCode()).isEqualTo(original.hashCode());
    }

    @Test
    void testEmptyAddressTextAndOf() {
        Address original = Address.builder().build();
        Address copy = Address.of(original.text());
        assertThat(copy).isEqualTo(original);
        assertThat(copy.hashCode()).isEqualTo(original.hashCode());
    }
}
