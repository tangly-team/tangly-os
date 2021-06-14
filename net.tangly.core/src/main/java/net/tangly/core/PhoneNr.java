/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

import java.lang.invoke.MethodHandles;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an immutable phone number as a canonical string. Validation and formatting of phone numbers is performed through the phone library of Google which
 * supports worldwide numbers.
 */
public record PhoneNr(@NotNull String number) {
    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public PhoneNr {
        if (!isValid(number)) {
            throw new IllegalArgumentException("Illegal phone number " + number);
        }
    }

    /**
     * Factory method to of a new phone number. The number is formatted based on the international format standard.
     *
     * @param number phone number in raw format
     * @return the newly created phone number if the raw format contained a number otherwise null
     */
    public static PhoneNr of(@NotNull String number) {
        if (!Strings.isNullOrBlank(number)) {
            PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber googleNr = numberUtil.parse(number, "CH");
                return new PhoneNr(numberUtil.format(googleNr, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));
            } catch (NumberParseException |IllegalArgumentException e) {
                logger.atWarn().withThrowable(e).log("Error creating phone number {}", number);
            }
        }
        return null;
    }

    /**
     * Validates a phone number using the Google phone library.
     *
     * @param number phone number to validate
     * @return true if the phone number is valid otherwise false
     */
    public static boolean isValid(String number) {
        PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = numberUtil.parse(number, "CH");
            return numberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            logger.atWarn().withThrowable(e).log("Error validating phone number {}", number);
            return false;
        }
    }
}
