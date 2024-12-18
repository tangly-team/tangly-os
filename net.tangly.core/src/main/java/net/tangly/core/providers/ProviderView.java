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

import java.util.List;
import java.util.function.Predicate;

/**
 * Define a filtered view on an underlying provider. The view is defined by a predicate on the items of the provider.
 *
 * @param <T> type of the items handled in the provider
 */
public class ProviderView<T> extends Provider<T> {
    private final Provider<T> provider;
    private Predicate<T> predicate;

    public ProviderView(@NotNull Provider<T> provider, @NotNull Predicate<T> predicate) {
        this.provider = provider;
        this.predicate = predicate;
    }

    public static <T> Provider<T> of(@NotNull Provider<T> provider) {
        return of(provider, o -> true);
    }

    public static <T> Provider<T> of(@NotNull Provider<T> provider, @NotNull Predicate<T> predicate) {
        return new ProviderView<>(provider, predicate);
    }

    public void predicate(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public List<T> items() {
        return provider.items().stream().filter(predicate).toList();
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
