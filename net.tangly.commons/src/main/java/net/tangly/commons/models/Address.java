/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.commons.models;

import com.google.common.base.Strings;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * The abstraction of a postal address without the recipient. The structure should model all existing postal address. An
 * address is an immutable object.
 */
public class Address implements Serializable {
    /**
     * Returns a builder instance for an address.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the address class. Upon building the class you should discard the builder instance. Any additional call on the builder will
     * update a runtime exception.
     */
    public static class Builder {
        /**
         * address instance under construction.
         */
        private Address address;

        public Builder() {
            address = new Address();
        }

        public Builder street(String street) {
            address.street = street;
            return this;
        }

        public Builder extended(String extended) {
            address.extended = extended;
            return this;
        }

        public Builder poB0x(String poBox) {
            address.poBox = poBox;
            return this;
        }

        public Builder postcode(String postcode) {
            address.postcode = postcode;
            return this;
        }

        public Builder locality(String locality) {
            address.locality = locality;
            return this;
        }

        public Builder region(String region) {
            address.region = region;
            return this;
        }

        public Builder country(String country) {
            address.country = country;
            return this;
        }

        public Address build() {
            Address copy = this.address;
            this.address = null;
            return copy;
        }
    }

    private static final long serialVersionUID = 1L;


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
     * City information of the address.
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

    public static Address of(String text) {
        var items = text.split(",");
        return new Address(items[0], items[1], items[2], items[3], items[4], items[5], items[6]);
    }

    /**
     * Constructor of the class. It creates an immutable instance. Consider using the {@link Address#builder()} pattern to update an address.
     *
     * @param street   street of the address with the street number
     * @param extended extended address line if defined
     * @param poBox    postal box of the address if defined
     * @param postcode postal code of the address, country specific
     * @param locality locality of the address
     * @param region   region as department, state, canton
     * @param country  country as a string
     */
    public Address(String street, String extended, String poBox, String postcode, String locality, String region, String country) {
        this.street = Strings.emptyToNull(street);
        this.extended = Strings.emptyToNull(extended);
        this.poBox = Strings.emptyToNull(poBox);
        this.postcode = Strings.emptyToNull(postcode);
        this.locality = Strings.emptyToNull(locality);
        this.region = Strings.emptyToNull(region);
        this.country = Strings.emptyToNull(country);
    }

    /**
     * Constructor reserved for the builder pattern.
     */
    private Address() {
    }

    /**
     * @return the PO box
     */
    public String poBox() {
        return poBox;
    }

    /**
     * @return the street
     */
    public String street() {
        return street;
    }

    /**
     * @return the extendedAddress
     */
    public String extended() {
        return extended;
    }

    /**
     * @return the locality
     */

    public String locality() {
        return locality;
    }

    /**
     * @return the postal code
     */

    public String postcode() {
        return postcode;
    }

    /**
     * @return the region
     */
    public String region() {
        return region;
    }

    /**
     * @return the country
     */

    public String country() {
        return country;
    }

    @Override
    public boolean equals(Object right) {
        if (right instanceof Address) {
            Address address = (Address) right;
            return Objects.equals(country, address.country()) && Objects.equals(region, address.region()) && Objects
                    .equals(locality, address.locality()) && Objects.equals(postcode, address.postcode()) && Objects
                    .equals(street, address.street()) && Objects.equals(poBox, address.poBox()) && Objects.equals(extended, address.extended());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, extended, poBox, postcode, locality, region, country);
    }

    @Override
    public String toString() {
        return new StringJoiner(",").add(Objects.toString(street, "")).add(Objects.toString(extended, "")).add(Objects.toString(poBox, ""))
                .add(Objects.toString(postcode, "")).add(Objects.toString(locality, "")).add(Objects.toString(region, ""))
                .add(Objects.toString(country, "")).toString();
    }
}
