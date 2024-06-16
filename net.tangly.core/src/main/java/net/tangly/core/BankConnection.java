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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iban4j.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Describes a bank connection with IBAN account number, BIC identification and name of the institute. The class is immutable.
 */
public record BankConnection(@NotNull String iban, String bic, String institute) {
    private static final Logger logger = LogManager.getLogger();

    public BankConnection {
        if (Strings.isNullOrBlank(iban) || !validateIban(iban)) {
            throw new IllegalArgumentException(STR."Illegal IBAN number \{iban}");
        }
        if (!Strings.isNullOrBlank(bic) && !validateBic(bic)) {
            throw new IllegalArgumentException(STR."Illegal BIC number \{bic}");
        }
    }

    public BankConnection(@NotNull String iban) {
        this(iban, null, null);
    }

    public BankConnection(@NotNull String iban, String bic) {
        this(iban, bic, null);
    }

    /**
     * Factory method to construct a bank connection.
     *
     * @param iban      iban of the bank connection
     * @param bic       bic of the bank connection
     * @param institute institute of the bank connection
     * @return new bank connection
     */
    public static BankConnection of(@NotNull String iban, String bic, String institute) {
        try {
            return new BankConnection(iban, bic, institute);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Build a bank connection object from a comma separated string representation.
     *
     * @param text comma separated representation of the address instance
     * @return new address object
     * @see BankConnection#text()
     */
    public static BankConnection of(@NotNull String text) {
        var parts = Objects.requireNonNull(text).split(",", -1);
        Objects.checkFromIndexSize(0, parts.length, 3);
        try {
            return new BankConnection(Strings.normalizeToNull(parts[0]), Strings.normalizeToNull(parts[1]), Strings.normalizeToNull(parts[2]));
        } catch (IllegalArgumentException e) {
            logger.atWarn().withThrowable(e).log("Error creating bank connection {}", text);
            return null;

        }
    }

    /**
     * Validate a string representing a formatted iban number.
     *
     * @param iban iban number to validate
     * @return true, if iban is valid otherwise false
     */
    public static boolean validateIban(@NotNull String iban) {
        try {
            IbanUtil.validate(iban, IbanFormat.Default);
            return true;
        } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
            logger.atWarn().withThrowable(e).log("Error validating IBAN {}", iban);
            return false;
        }
    }

    /**
     * Validate a string representing a formatted bic number.
     *
     * @param bic bic number to validate
     * @return true, if the bic is valid otherwise false
     */
    public static boolean validateBic(String bic) {
        try {
            BicUtil.validate(bic);
            return true;
        } catch (BicFormatException | UnsupportedCountryException e) {
            logger.atWarn().withThrowable(e).log("Error validating BIC {}", bic);
            return false;
        }
    }

    /**
     * Return a comma-separated representation of a bank connection. Null values are shown as empty strings. The {@link Object#toString()} method is not used because the
     * implementation is defined in the API implementation of record construct. The generated string can be feed to the {@link BankConnection#of(String)} to create a bank
     * connection object.
     *
     * @return comma separated representation
     * @see BankConnection#of(String)
     */
    public String text() {
        return String.format("%s,%s,%s", Strings.nullToEmpty(iban()), Strings.nullToEmpty(bic()), Strings.nullToEmpty(institute()));
    }

}
