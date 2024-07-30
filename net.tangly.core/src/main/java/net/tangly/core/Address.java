/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core;

import net.tangly.commons.lang.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Address immutability is enforced through a record construct. The street represents the international concept of house identifier often means street name and street number. It
 * can also be the street name and house name such as in Ireland or Great Britain.
 *
 * @param street   street and street number of the address
 * @param extended extended address line of the address
 * @param poBox    optional postal box code of the address
 * @param postcode zip code of the address as custom in the country
 * @param locality mandatory locality of the address
 * @param region   region of the address as custom in the country
 * @param country  mandatory country of the address as ISO 2 characters code
 */
public record Address(String street, String extended, String poBox, String postcode, String locality, String region, String country) {
    public Address {
        if (Strings.isNullOrBlank(country)) {
            throw new IllegalArgumentException("Illegal country %s".formatted(country));
        }
        if (Strings.isNullOrBlank(locality)) {
            throw new IllegalArgumentException("Illegal locality %s".formatted(locality));
        }
    }

    /**
     * Returns a builder instance for an address object. The builder can be used to create multiple address objects
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a comma-separated representation of an address. Null values are shown as empty strings. The {@link Object#toString()} method is not used
     * because the implementation is defined in the API implementation of record construct. The generated string can be feed to the
     * {@link Address#of(String)} to create an address object.
     *
     * @return comma separated representation
     * @see Address#of(String)
     */
    public String text() {
        return String.format("%s,%s,%s,%s,%s,%s,%s", Strings.nullToEmpty(street), Strings.nullToEmpty(extended), Strings.nullToEmpty(poBox),
            Strings.nullToEmpty(postcode), Strings.nullToEmpty(locality), Strings.nullToEmpty(region), Strings.nullToEmpty(country));
    }

    /**
     * Builds an address object from a comma separated string representation.
     *
     * @param text comma separated representation of the address instance
     * @return new address object
     * @see Address#text()
     */
    public static Address of(@NotNull String text) {
        var parts = Objects.requireNonNull(text).split(",", -1);
        Objects.checkFromIndexSize(0, parts.length, 7);
        return builder().street(parts[0]).extended(parts[1]).poBox(parts[2]).postcode(parts[3]).locality(parts[4]).region(parts[5]).country(parts[6]).build();
    }

    /**
     * Defines the builder for the address class with a fluent interface. The builder supports the creation of multiple address objects. A canonical
     * transformation is used to
     * transform empty or blank {@link String#isBlank} string fields into null values.
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
         * Postal number for the address.
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
        private String country;

        public Builder street(String street) {
            this.street = Strings.normalizeToNull(street);
            return this;
        }

        public Builder extended(String extended) {
            this.extended = Strings.normalizeToNull(extended);
            return this;
        }

        public Builder poBox(String poBox) {
            this.poBox = Strings.normalizeToNull(poBox);
            return this;
        }

        public Builder postcode(String postcode) {
            this.postcode = Strings.normalizeToNull(postcode);
            return this;
        }

        public Builder locality(String locality) {
            this.locality = Strings.normalizeToNull(locality);
            return this;
        }

        public Builder region(String region) {
            this.region = Strings.normalizeToNull(region);
            return this;
        }

        public Builder country(String country) {
            this.country = Strings.normalizeToNull(country);
            return this;
        }

        public Address build() {
            return new Address(street, extended, poBox, postcode, locality, region, country);
        }
    }
}
