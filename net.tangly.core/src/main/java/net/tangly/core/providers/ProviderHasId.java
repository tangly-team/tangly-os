/*
 * Copyright 2021-2024 Marcel Baumann
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
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * The class is a decorator for a provider with entities having a unique object identifier which should be handled through the provider.
 *
 * @param <T> type of the items handled in the provider
 */
public class ProviderHasId<T extends HasId> implements Provider<T> {
    private final Provider<T> provider;

    public ProviderHasId(@NotNull Provider<T> provider) {
        this.provider = provider;
    }

    public static <T extends HasId> Provider<T> of(@NotNull Iterable<T> items) {
        return new ProviderHasId<>(ProviderInMemory.of(items));
    }

    public static <T extends HasId> Provider<T> of(@NotNull EmbeddedStorageManager storageManager, @NotNull List<T> items) {
        return new ProviderHasId<>(ProviderPersistence.of(storageManager, items));
    }

    /**
     * Checks if the entity can be added through the provider. The entity can be added either if no entity with the same oid is already stored in the provider, or the stored
     * entity and the entity we shall add is the same Java object.
     *
     * @param entity entity to be added
     * @return flag indicating if it is allowed to add the object
     */
    public boolean contains(@NotNull T entity) {
        Optional<T> original = Provider.findById(provider, entity.id());
        return (original.isPresent() && (original.get().equals(entity)));
    }

    @Override
    public List<T> items() {
        return provider.items();
    }

    @Override
    public void update(@NotNull T entity) {
        provider.update(entity);
    }

    @Override
    public void delete(@NotNull T entity) {
        provider.delete(entity);
    }

    @Override
    public void deleteAll() {
        provider.deleteAll();
    }
}
