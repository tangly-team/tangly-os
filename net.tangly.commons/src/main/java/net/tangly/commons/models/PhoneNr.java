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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import java.io.Serializable;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Immutable class representing a phone number. We use the google library to parse and validate
 * phone numbers (http://code.google.com/p/libphonenumber). A phone number is an immutable object.
 */
public class PhoneNr implements Serializable {

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
        if (!Strings.isNullOrEmpty(number)) {
            return new PhoneNr(number);
        } else {
            return null;
        }
    }

    /**
     * Constructor of the class.
     *
     * @param number textual representation of a phone number
     */
    public PhoneNr(String number) {
        checkNotNull(number);
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
        return MoreObjects.toStringHelper(this).add("number", number).toString();
    }
}
