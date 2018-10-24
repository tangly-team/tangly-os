/*
 *
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.commons.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * The abstraction of a postal address without the recipient. The structure should model all existing postal address. An
 * address is an immutable object.
 */
public class Address implements Serializable {
    /**
     * Street information of the address including house number.
     */
    private final String street;

    /**
     * Extended information of the address such as floor and suite information.
     */
    private final String extended;

    /**
     * PO Box of the address.
     */
    private final String poBox;

    /**
     * Postal number of the address.
     */
    private final String postcode;

    /**
     * City information of the address.
     */
    private final String locality;

    /**
     * Region information of the address such as state, department.
     */
    private final String region;

    /**
     * Country information of the address.
     */
    private final String country;

    public static Address of(String text) {
        var items = text.split("(,)");
        return new Address(items[0], items[1], items[2], items[3], items[4], items[5], items[6]);
    }

    /**
     * Constructor of the class.
     *
     * @param street   street of the address with the street number
     * @param postcode postal code of the address, country specific
     * @param locality locality of the address
     * @param region   region as department, state, canton
     * @param country  country as a string
     */
    public Address(String street, String postcode, String locality, String region, String country) {
        this(street, null, null, postcode, locality, region, country);
    }

    /**
     * Constructor of the class. It creates an immutable instance.
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
        this.street = street;
        this.extended = extended;
        this.poBox = poBox;
        this.postcode = postcode;
        this.locality = locality;
        this.region = region;
        this.country = country;
    }

    /**
     * @return the PO box
     */
    public String getPoBox() {
        return poBox;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @return the extendedAddress
     */
    public String getExtended() {
        return extended;
    }

    /**
     * @return the locality
     */

    public String getLocality() {
        return locality;
    }

    /**
     * @return the postal code
     */

    public String getPostcode() {
        return postcode;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @return the country
     */

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object right) {
        if (right instanceof Address) {
            Address address = (Address) right;
            return Objects.equals(country, address.getCountry()) && Objects.equals(region, address.getRegion()) && Objects
                    .equals(locality, address.getLocality()) && Objects.equals(postcode, address.getPostcode()) && Objects
                    .equals(street, address.getStreet()) && Objects.equals(poBox, address.getPoBox()) && Objects
                    .equals(extended, address.getExtended());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, extended, poBox, postcode, locality, region, country);
    }

    @Override
    public String toString() {
        return new StringJoiner("; ", this.getClass().getSimpleName() + "(", ")").add(Objects.toString(street, ""))
                .add(Objects.toString(extended, "")).add(Objects.toString(poBox, "")).add(Objects.toString(postcode, ""))
                .add(Objects.toString(locality, "")).add(Objects.toString(region, "")).add(Objects.toString(country, "")).toString();
    }
}
