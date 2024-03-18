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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class ProviderPersistenceTest implements ProviderTest {
    static class Data {
        private List<Aggregate> aggregates;

        Data() {
            aggregates = new ArrayList<>();
        }

    }

    @Test
    void dummy() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Files.createDirectory(fs.getPath("/var/"));
            Data data = new Data();
            EmbeddedStorageManager storageManager = EmbeddedStorage.start(data, fs.getPath("/var/", "data"));
            ProviderPersistence<Aggregate> provider = ProviderPersistence.of(storageManager, data.aggregates);
            provider.updateAll(ProviderTest.aggregates());
            storageManager.close();
        }
    }
}
