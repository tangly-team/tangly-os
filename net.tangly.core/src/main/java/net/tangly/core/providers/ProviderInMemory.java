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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provider where all instances are in memory. No persistence is provided.
 *
 * @param <T> type of the instances handled in the provider
 */
public class ProviderInMemory<T> extends Provider<T> {
    private final List<T> items;

    public ProviderInMemory() {
        items = new ArrayList<>();
    }

    public static <T> Provider<T> of() {
        return new ProviderInMemory<>();
    }

    public static <T> Provider<T> of(@NotNull Iterable<? extends T> items) {
        Provider<T> provider = new ProviderInMemory<>();
        provider.updateAll(items);
        return provider;
    }

    @Override
    public List<T> items() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void update(@NotNull T entity) {
        if (!items.contains(entity)) {
            items.add(entity);
        }
    }

    @Override
    public void delete(@NotNull T entity) {
        items.remove(entity);
    }

    @Override
    public void deleteAll() {
        items.clear();
    }
}
