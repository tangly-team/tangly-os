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

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represents an immutable phone number as a canonical string.
 */
public record PhoneNr(String number) implements Serializable {
    /**
     * pattern used to normalize the phone numbers.
     */
    private static final Pattern PATTERN = Pattern.compile("(\\(0\\)| |-|[^\\d+])");

    /**
     * Factory method to of a new phone number.
     *
     * @param number phone number in raw format
     * @return the newly created phone number if the raw format contained a number otherwise null
     */
    public static PhoneNr of(String number) {
        return Strings.isNullOrEmpty(number) ? null : new PhoneNr(PATTERN.matcher(number).replaceAll(""));
    }
}
