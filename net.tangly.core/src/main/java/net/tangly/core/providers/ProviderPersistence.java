/*
 * Copyright 2006-2022 Marcel Baumann
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

import one.microstream.persistence.binary.jdk17.types.BinaryHandlersJDK17;
import one.microstream.persistence.binary.jdk8.types.BinaryHandlersJDK8;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Provider with instances cached in memory and persisted onto the file system or a database.
 * <p>The update method uses an eager storage strategy to insure that all instance variables of a Java object are persisted. This approach is necessary due
 * to the implementation restrictions of MicroStream. The current regular store operation does not persist fields based on collections.</p>
 *
 * @param <T> type of the instances handled in the provider
 */
public class ProviderPersistence<T> implements Provider<T> {
    private final EmbeddedStorageManager storageManager;
    private final List<T> items;

    public ProviderPersistence(@NotNull EmbeddedStorageManager storageManager, List<T> items) {
        final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation();
        foundation.onConnectionFoundation(BinaryHandlersJDK8::registerJDK8TypeHandlers);
        foundation.onConnectionFoundation(BinaryHandlersJDK17::registerJDK17TypeHandlers);
        this.storageManager = storageManager;
        this.items = items;
    }

    public static <T> ProviderPersistence<T> of(@NotNull EmbeddedStorageManager storageManager, List<T> items) {
        return new ProviderPersistence<>(storageManager, items);
    }

    @Override
    public List<T> items() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void update(@NotNull T entity) {
        if (!items.contains(entity)) {
            items.add(entity);
            storageManager.store(items);
        } else {
            var storage = storageManager.createEagerStorer();
            storage.store(entity);
            storage.commit();
        }
    }

    @Override
    public void updateAll(@NotNull Iterable<? extends T> entities) {
        entities.forEach(entity -> {
            if (!items.contains(entity)) {
                items.add(entity);
            } else {
                storageManager.store(entity);
            }
        });
        storageManager.store(items);
    }

    @Override
    public void delete(@NotNull T entity) {
        items.remove(entity);
    }
}
