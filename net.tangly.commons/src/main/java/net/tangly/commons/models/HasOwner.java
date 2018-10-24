package net.tangly.commons.models;

/**
 * The interface marks an entity owned by another object.
 */
public interface HasOwner {
    /**
     * Returns the unique object identifier of the owning object.
     *
     * @return owning object identifier if defined otherwise {@link HasId#UNDEFINED_OID}
     */
    long foid();


    /**
     * Sets the unique object identifier of the owning object.
     *
     * @param foid object identifier of the owing object if defined otherwise {@link HasId#UNDEFINED_OID}
     */
    void foid(long foid);
}