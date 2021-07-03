/*
 * Copyright 2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.providers;

import java.util.List;
import java.util.Optional;

import net.tangly.commons.generator.IdGenerator;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.core.HasOid;
import one.microstream.storage.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

/**
 * The class is a decorator for a provider with entities having a unique object identifier which should be handled through the provider.
 *
 * @param <T> type of the items handled in the provider
 */
public class ProviderHasOid<T extends HasOid> implements Provider<T> {
    private final IdGenerator generator;
    private final Provider<T> provider;

    public ProviderHasOid(@NotNull IdGenerator generator, @NotNull Provider<T> provider) {
        this.generator = generator;
        this.provider = provider;
    }

    public static <T extends HasOid> Provider<T> of(@NotNull IdGenerator generator) {
        return new ProviderHasOid<>(generator, ProviderInMemory.of());
    }

    public static <T extends HasOid> Provider<T> of(@NotNull IdGenerator generator, @NotNull Iterable<? extends T> items) {
        return new ProviderHasOid<>(generator, ProviderInMemory.of(items));
    }

    public static <T extends HasOid> Provider<T> of(@NotNull IdGenerator generator, @NotNull EmbeddedStorageManager storageManager, @NotNull List<T> items) {
        return new ProviderHasOid<>(generator, ProviderPersistence.of(storageManager, items));
    }

    /**
     * Checks if the entity can be added through the provider. The entity can be added either if no entity with samoe oid is already stored in the provider or
     * the stored entity and the entity we shall add are the same Java object.
     *
     * @param entity entity to be added
     * @return flag indicating if it is allowed to add the object
     */
    public boolean canBeAdded(@NotNull T entity) {
        Optional<T> original = Provider.findByOid(provider, entity.oid());
        return (original.isEmpty() || (original.get() == entity));
    }

    @Override
    public List<T> items() {
        return provider.items();
    }

    @Override
    public void update(@NotNull T entity) {
        if (entity.oid() == HasOid.UNDEFINED_OID) {
            ReflectionUtilities.set(entity, "oid", generator.id());
        }
        if (canBeAdded(entity)) {
            provider.update(entity);
        } else {
            throw new IllegalArgumentException("Different Objects with duplicate oid " + entity.toString());
        }
    }

    @Override
    public void delete(@NotNull T entity) {
        provider.delete(entity);
    }
}
