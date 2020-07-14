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

package net.tangly.commons.orm;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.tangly.bus.core.HasOid;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the abstraction to handle all instances of a business model abstraction type.
 *
 * @param <T> type of the business model abstraction
 */
public interface InstanceProvider<T extends HasOid> {
    /**
     * Find the entity instance with the given unique object identifier.
     *
     * @param oid object identifier of the instance to find
     * @return optional of the requested entity
     */
    Optional<T> find(long oid);

    /**
     * Return a list containing all known instances of the entity type.
     *
     * @return list of all instances
     */
    List<T> getAll();

    /**
     * Update the data associated with the entity. If the entity is new the update is handled as a create operation. The update is transitive and all
     * referenced entities are also updated. The entity given as parameter becomes the instance managed through the provider.
     *
     * @param entity entity to update
     */
    void update(@NotNull T entity);

    /**
     * Delete the data associated with the entity. The object identifier is invalidated.
     *
     * @param entity entity to deleate
     */
    void delete(@NotNull T entity);


    /**
     * Update the data associated with all entities. See {@link InstanceProvider#update(HasOid)}.
     *
     * @param items entities to update
     */
    default void updateAll(@NotNull Collection<T> items) {
        items.forEach(this::update);
    }
}
