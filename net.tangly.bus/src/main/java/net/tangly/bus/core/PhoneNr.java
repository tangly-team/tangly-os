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
