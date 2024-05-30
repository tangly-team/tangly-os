/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.app.ports;

import net.tangly.app.domain.User;
import net.tangly.app.services.AppsRealm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AppsEntities implements AppsRealm {
    static class Data {
        final List<User> users;

        Data() {
            users = new ArrayList<>();
        }
    }

    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<User> users;
    private final EmbeddedStorageManager storageManager;

    public AppsEntities(@NotNull Path path) {
        data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        users = ProviderPersistence.of(storageManager, data.users);
    }

    public AppsEntities() {
        data = new Data();
        storageManager = null;
        users = ProviderInMemory.of(data.users);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    @Override
    public Provider<User> users() {
        return users;
    }
}

