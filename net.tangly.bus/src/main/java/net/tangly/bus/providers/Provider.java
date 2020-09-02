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

package net.tangly.bus.providers;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the provider abstraction responsible to handle all instances of a specific type.
 *
 * @param <T> type of the instances
 */
public interface Provider<T> {
    /**
     * Return a list containing all known instances of the entity type.
     *
     * @return list of all instances
     */
    List<T> items();

    /**
     * Update the data associated with the entity. If the entity is new the update is handled as a create operation. The update is transitive and all referenced
     * entities are also updated. The entity given as parameter becomes the instance managed through the provider.
     *
     * @param entity entity to update
     */
    void update(@NotNull T entity);

    /**
     * Delete the data associated with the entity. The object identifier is invalidated.
     *
     * @param entity entity to delete
     */
    void delete(@NotNull T entity);

    /**
     * Return the first entity which property matches the value.
     *
     * @param getter getter to retrieve the property
     * @param value  value to compare with
     * @param <U>    type of the property
     * @return optional of the first matching entity otherwise empty
     */
    default <U> Optional<T> findBy(Function<T, U> getter, U value) {
        return items().stream().filter(o -> value.equals(getter.apply(o))).findAny();
    }

    /**
     * Update the data associated with all entities.
     *
     * @param items entities to update
     */
    default void updateAll(@NotNull Iterable<? extends T> items) {
        items.forEach(this::update);
    }
}
