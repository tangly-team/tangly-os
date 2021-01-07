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

package net.tangly.core;

import java.util.Objects;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

/**
 * The abstraction of an email address until the Java JDK provides one..
 */
public record EmailAddress(@NotNull String recipient, @NotNull String domain) {
    private static final String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(emailRegex);

    public static EmailAddress of(@NotNull String email) {
        String[] parts = Objects.requireNonNull(email).split("@");
        Objects.checkFromIndexSize(0, parts.length, 2);
        return new EmailAddress(Strings.normalizeToNull(parts[0]), Strings.normalizeToNull(parts[1]));
    }

    public EmailAddress {
        if (Strings.isNullOrBlank(domain)) {
            throw new IllegalArgumentException("Illegal domain " + domain);
        }
        if (Strings.isNullOrBlank(recipient)) {
            throw new IllegalArgumentException("Illegal recipient " + recipient);
        }
    }

    public static boolean isValid(String email) {
        return !Strings.isNullOrBlank(email) && pattern.matcher(email).matches();
    }

    /**
     * Returns a text representation of an email address. The {@link Object#toString()} method is not used * because the implementation is defined in the API
     * implementation of record construct. The generated string can be feed to the {@link * EmailAddress#of(String)} to create an email address object.
     *
     * @return text representation
     */
    public String text() {
        return recipient() + "@" + domain();
    }
}
