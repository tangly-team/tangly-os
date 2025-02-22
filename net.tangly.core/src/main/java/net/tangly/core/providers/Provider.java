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

package net.tangly.core.providers;

import net.tangly.core.HasId;
import net.tangly.core.HasOid;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Define the provider abstraction responsible for handling instances of a specific type. The provider declares the regular CRUD operations: <i>Create, Read, Update, and
 * Delete</i>. The provider is the repository and often the factory in the domain-driven design terminology.
 * <p>You should store domain-driven domain aggregates in a provider. Each aggregate should have a root with an identifier. All objects of an aggregate are owned by the
 * aggregate and are always edited through the root. The concept defines a natural job and transactional boundary.</p>
 * <dl>
 *     <dt>Create</dt><dd>The creation operation is integrated with the update operation {@link Provider#update(Object)}</dd>
 *     <dt>Read</dt><dd>The read all items operation maps to the {@link Provider#items()} operation.
 *     The read an item with a unique key operation maps to {@link Provider#findBy(Function, Object)}. </dd>
 *     <dt>Update</dt><dd>The update operation maps to the operation {@link Provider#update(Object)}. </dd>
 *     <dt>Delete</dt><dd>The delete operation maps to the operation {@link Provider#delete(Object)}.</dd>
 * </dl>
 *
 * @param <T> type of the instances
 */
public abstract class Provider<T> {
    private final ReentrantReadWriteLock mutex;

    public Provider() {
        this.mutex = new ReentrantReadWriteLock();
    }

    public Provider(@NotNull ReentrantReadWriteLock mutex) {
        this.mutex = mutex;
    }

    public static <E extends HasOid, Long> Optional<E> findByOid(@NotNull Provider<E> provider, long oid) {
        return provider.findBy(E::oid, oid);
    }

    public static <E extends HasId, String> Optional<E> findById(@NotNull Provider<E> provider, @NotNull String id) {
        return provider.findBy(E::id, id);
    }

    public static boolean containsById(@NotNull Provider<? extends HasId> provider, @NotNull String id) {
        return findById(provider, id).isPresent();
    }

    /**
     * Returns a list containing all known instances of the entity type.
     *
     * @return list of all instances
     */
    public abstract List<T> items();

    /**
     * Updates the data associated with the entity. If the entity is new, the update is handled as a create operation. The update is transitive and all
     * referenced entities are also updated. The entity given as a parameter becomes the instance managed through the provider.
     *
     * @param entity entity to update
     */
    public abstract void update(@NotNull T entity);

    /**
     * Deletes the data associated with the entity. The object identifier is invalidated.
     *
     * @param entity entity to delete
     */
    public abstract void delete(@NotNull T entity);

    /**
     * Deletes all the entities managed by the provider.
     */
    public abstract void deleteAll();

    /**
     * Replaces an existing value with a new one. A null value is ignored.
     *
     * @param oldValue remove the old value if not null
     * @param newValue add the new value if not null
     */
    public void replace(T oldValue, T newValue) {
        execute(() -> {
            if (Objects.nonNull(oldValue)) {
                delete(oldValue);
            }
            if (Objects.nonNull(newValue)) {
                update(newValue);
            }
        });
    }

    /**
     * Updates the data associated with all entities.
     *
     * @param items entities to update
     */
    public void updateAll(@NotNull Iterable<? extends T> items) {
        items.forEach(this::update);
    }

    /**
     * Returns the first entity which property matches the value.
     *
     * @param getter getter to retrieve the property
     * @param value  value to compare with
     * @param <U>    type of the property
     * @return optional of the first matching entity otherwise empty
     */
    public <U> Optional<T> findBy(@NotNull Function<T, U> getter, U value) {
        return items().stream().filter(o -> value.equals(getter.apply(o))).findAny();
    }

    protected ReentrantReadWriteLock mutex() {
        return mutex;
    }
    protected void execute(@NotNull Runnable runnable) {
        mutex.writeLock().lock();
        try {
            runnable.run();
        } finally {
            mutex.writeLock().unlock();
        }
    }

}
