/*
 * Copyright 2023-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core.providers;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProviderTransaction<T> implements Provider<T> {
    private final Provider<T> provider;
    private final ReentrantReadWriteLock mutex;

    public ProviderTransaction(@NotNull Provider<T> provider) {
        this.provider = provider;
        this.mutex = new ReentrantReadWriteLock();
    }

    public void execute(@NotNull Runnable runnable) {
        mutex.writeLock().lock();
        try {
            runnable.run();
        } finally {
            mutex.writeLock().unlock();
        }
    }

    @Override
    public List<T> items() {
        return provider.items();
    }

    @Override
    public void update(@NotNull T entity) {
        execute(() -> update(entity));
    }

    @Override
    public void delete(@NotNull T entity) {
        execute(() -> delete(entity));
    }

    @Override
    public void deleteAll() {
        execute(this::deleteAll);
    }
}
