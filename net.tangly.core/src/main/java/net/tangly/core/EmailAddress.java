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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The abstraction of an email address until the Java JDK provides one.
 */
public record EmailAddress(@NotNull String recipient, @NotNull String domain) {
    private static final Logger logger = LogManager.getLogger();
    private static final String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(emailRegex);

    public EmailAddress {
        if (Strings.isNullOrBlank(domain)) {
            throw new IllegalArgumentException("Illegal domain %s".formatted(domain));
        }
        if (Strings.isNullOrBlank(recipient)) {
            throw new IllegalArgumentException("Illegal recipient %s".formatted(recipient));
        }
    }

    public static EmailAddress of(String email) {
        if (Strings.isNullOrBlank(email)) {
            return null;
        } else {
            String[] parts = Objects.requireNonNull(email).split("@");
            try {
                Objects.checkFromIndexSize(0, parts.length, 2);
                return new EmailAddress(Strings.normalizeToNull(parts[0]), Strings.normalizeToNull(parts[1]));
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                logger.atWarn().withThrowable(e).log("Error creating email address {}", email);
                return null;
            }
        }
    }

    public static boolean isValid(String email) {
        return !Strings.isNullOrBlank(email) && pattern.matcher(email).matches();
    }

    /**
     * Returns a text representation of an email address. The {@link Object#toString()} method is not used because the implementation is defined in the API
     * implementation of record
     * construct. The generated string can be feed to the {@link EmailAddress#of(String)} to create an email address object.
     *
     * @return text representation
     */
    public String text() {
        return "%s@%s".formatted(recipient(), domain());
    }
}
