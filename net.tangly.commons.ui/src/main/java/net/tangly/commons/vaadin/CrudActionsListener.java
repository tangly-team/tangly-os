package net.tangly.commons.vaadin;

import com.vaadin.flow.data.provider.DataProvider;

/**
 * The listener to react on CRUD component action completions.
 * @param <T> type of the entities handled in the listener
 */
public interface CrudActionsListener<T> {
    /**
     * Called when an entity is added to the list of entities in the CRUD component.
     * @param dataProvider data provider of the CRUD component
     * @param entity entity added
     */
    default void entityAdded(DataProvider<T, ?> dataProvider, T entity) {
    }

    /**
     * Called when an entity is deleted to the list of entities in the CRUD component.
     * @param dataProvider data provider of the CRUD component
     * @param entity entity deleted
     */
    default void entityDeleted(DataProvider<T, ?> dataProvider, T entity) {
    }

    /**
     * Called when an entity is updated to the list of entities in the CRUD component.
     * @param dataProvider data provider of the CRUD component
     * @param entity entity updated
     */
    default void entityUpdated(DataProvider<T, ?> dataProvider, T entity) {
    }
}
