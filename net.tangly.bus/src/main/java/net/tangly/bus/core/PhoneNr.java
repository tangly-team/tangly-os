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

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * Immutable class representing a phone number. We use the google library to of and validate phone numbers (http://code.google.com/p/libphonenumber).
 * A phone number is an immutable object.
 */
public class PhoneNr implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * pattern used to normalize the phone numbers.
     */
    private static final Pattern PATTERN = Pattern.compile("(\\(0\\)| |-|[^\\d+])");
    /**
     * normalized phone number.
     */
    private final String number;

    /**
     * Factory method to of a new phone number.
     *
     * @param number phone number in raw format
     * @return the newly created phone number if the raw format contained a number otherwise null
     */
    public static PhoneNr of(String number) {
        return Strings.isNullOrEmpty(number) ? null : new PhoneNr(number);
    }

    /**
     * Constructor of the class.
     *
     * @param number textual representation of a phone number
     */
    public PhoneNr(String number) {
        Objects.requireNonNull(number);
        this.number = PATTERN.matcher(number).replaceAll("");
    }

    /**
     * Returns the textual representation of a phone number.
     *
     * @return the normalized textual representation of a phone number.
     */
    public String getNumber() {
        return this.number;
    }

    @Override
    public boolean equals(Object right) {
        return right instanceof PhoneNr && number.equals(((PhoneNr) right).getNumber());
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public String toString() {
        return String.format("number=%s", number);
    }
}
