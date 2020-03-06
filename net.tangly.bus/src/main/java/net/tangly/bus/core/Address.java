/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.bus.core;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * The abstraction of a postal address without the recipient. The structure should model all existing postal address. An address is an immutable
 * object. The street represents the international concept of house identifier often meaning street name and street number. It can also be street name
 * and house name such as in Ireland or Great Britain.
 */
public record Address(String street, String extended, String poBox, String postcode, String locality, String region, String countryCode)
        implements Serializable {
    /**
     * Returns a builder instance for an address.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the address class. Upon building the class you should discard the builder instance. Any additional call on the builder will update
     * a runtime exception.
     */
    public static class Builder {
        /**
         * Street information of the address including house number.
         */
        private String street;

        /**
         * Extended information of the address such as floor and suite information.
         */
        private String extended;

        /**
         * PO Box of the address.
         */
        private String poBox;

        /**
         * Postal number of the address.
         */
        private String postcode;

        /**
         * locality of the address.
         */
        private String locality;

        /**
         * Region information of the address such as state, department.
         */
        private String region;

        /**
         * Country information of the address.
         */
        private String countryCode;

        public Builder street(String street) {
            street = Strings.emptyToNull(street);
            return this;
        }

        public Builder extended(String extended) {
            extended = Strings.emptyToNull(extended);
            return this;
        }

        public Builder poBox(String poBox) {
            poBox = Strings.emptyToNull(poBox);
            return this;
        }

        public Builder postcode(String postcode) {
            postcode = Strings.emptyToNull(postcode);
            return this;
        }

        public Builder locality(String locality) {
            locality = Strings.emptyToNull(locality);
            return this;
        }

        public Builder region(String region) {
            region = Strings.emptyToNull(region);
            return this;
        }

        public Builder countryCode(String countryCode) {
            countryCode = Strings.emptyToNull(countryCode);
            return this;
        }

        public Address build() {
            return new Address(street, extended, poBox, postcode, locality, region, countryCode);
        }
    }

    public static Address of(@NotNull String text) {
        var parts = text.split(",");
        Objects.checkFromIndexSize(0, parts.length, 7);
        return builder().street(parts[0]).extended(parts[1]).poBox(parts[2]).postcode(parts[3]).locality(parts[4]).region(parts[5])
                .countryCode(parts[6]).build();
    }
}
