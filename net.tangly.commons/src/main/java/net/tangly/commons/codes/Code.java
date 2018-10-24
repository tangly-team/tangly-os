package net.tangly.commons.codes;

import java.io.Serializable;

/**
 * The abstraction of a code table value. A code table is an extensible and temporal enumeration. New values can be added without recompiling the
 * application. A code can have a validity period. To guaranty consistency a code value shall never be deleted, only be disabled.
 */
public interface Code extends Serializable {
    /**
     * Returns the unique identifier of a code table entity.
     *
     * @return unique identifier of the code table instance
     */
    int id();

    /**
     * Returns the human readable representation of a code table entity.
     *
     * @return human readable code
     */
    String code();

    /**
     * Returns true if the code table instance is enabled.
     *
     * @return flag indicating if the code is enabled or archived
     */
    boolean isEnabled();
}