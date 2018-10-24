package net.tangly.commons.models;

import java.io.Serializable;

/**
 * Mixin states that the class has an external unique object identifier and a human readable name. The interface
 * supports only querying the external identifier. It is the responsibility of the application to decide if the
 * external identifier can be modified or not.
 */
public interface HasId extends Serializable {
    /**
     * Place holder to identify an illegal or undefined internal identifier.
     */
    int UNDEFINED_OID = 0;

    /**
     * Returns the unique internal identifier of the instance.
     *
     * @return unique internal identifier
     */
    default long oid() {
        return UNDEFINED_OID;
    }

    /**
     * Returns the unique external and visible identifier of the instance.
     *
     * @return unique external identifier
     */
    String id();

    /**
     * Returns the human readable name of the instance. Per default it returns the external identifier.
     *
     * @return the human readable name of the instance
     */
    default String name() {
        return id();
    }

    /**
     * Sets the name of the entity. Per default it does nothing.
     *
     * @param name human readable name of the entity
     */
    default void name(String name) {
    }
}
