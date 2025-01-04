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

import org.eclipse.serializer.concurrency.LockedExecutor;
import org.eclipse.serializer.persistence.binary.jdk17.types.BinaryHandlersJDK17;
import org.eclipse.serializer.persistence.binary.jdk8.types.BinaryHandlersJDK8;
import org.eclipse.serializer.persistence.types.Storer;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageFoundation;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Provider where all instances are cached in memory and persisted onto the file system or a database.
 * <p>The update method uses an eager storage strategy to insure that all instance variables of a Java object are persisted. This approach is necessary due
 * to the implementation restrictions of MicroStream. The current regular store operation does not persist fields based on collections.</p>
 *
 * @param <T> type of the instances handled in the provider
 */
public class ProviderPersistence<T> extends Provider<T> {
    private final EmbeddedStorageManager storageManager;
    private final List<T> items;
    private final transient LockedExecutor executor = LockedExecutor.New();

    public ProviderPersistence(@NotNull EmbeddedStorageManager storageManager, @NotNull List<T> items) {
        final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation();
        foundation.onConnectionFoundation(BinaryHandlersJDK8::registerJDK8TypeHandlers);
        foundation.onConnectionFoundation(BinaryHandlersJDK17::registerJDK17TypeHandlers);
        this.storageManager = storageManager;
        this.items = items;
    }

    public static <T> ProviderPersistence<T> of(@NotNull EmbeddedStorageManager storageManager, @NotNull List<T> items) {
        return new ProviderPersistence<>(storageManager, items);
    }

    @Override
    public List<T> items() {
        try {
            mutex().readLock().lock();
            return Collections.unmodifiableList(items);
        } finally {
            mutex().readLock().unlock();
        }
    }

    @Override
    public void update(@NotNull T entity) {
        execute(() -> {
            if (!items.contains(entity)) {
                items.add(entity);
                storageManager.store(entity);
            } else {
                Storer storer = storageManager.createEagerStorer();
                storer.store(entity);
                storer.commit();
            }
            storageManager.store(items);
        });
    }

    @Override
    public void updateAll(@NotNull Iterable<? extends T> entities) {
        execute(() -> {
            Storer storer = storageManager.createEagerStorer();
            entities.forEach(entity -> {
                if (!items.contains(entity)) {
                    items.add(entity);
                } else {
                    storer.store(entity);
                }
            });
            storageManager.store(entities);
            storageManager.store(items);
            storer.commit();
        });
    }

    @Override
    public void delete(@NotNull T entity) {
        execute(() -> {
            items.remove(entity);
            storageManager.store(items);
        });
    }

    @Override
    public void deleteAll() {
        items.clear();
        storageManager.store(items);
    }
}
