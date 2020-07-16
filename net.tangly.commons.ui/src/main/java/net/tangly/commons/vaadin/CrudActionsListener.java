/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

/**
 * The listener to react on CRUD component action completions.
 *
 * @param <T> type of the entities handled in the listener
 */
public interface CrudActionsListener<T> {
    /**
     * Called when an entity is added to the list of entities in the CRUD component.
     *
     * @param entity entity added
     */
    void entityAdded(T entity);

    /**
     * Called when an entity is deleted to the list of entities in the CRUD component.
     *
     * @param entity entity deleted
     */
    void entityDeleted(T entity);

    /**
     * Called when an entity is updated to the list of entities in the CRUD component.
     *
     * @param entity entity updated
     */
    void entityUpdated(T entity);
}
