/*
 * Copyright 2022-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.collabortors.ports;

import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.collabortors.domain.Collaborator;
import net.tangly.erp.collabortors.domain.Contract;
import net.tangly.erp.collabortors.domain.Organization;
import net.tangly.erp.collabortors.services.CollaboratorsRealm;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CollaboratorsEntities implements CollaboratorsRealm {
    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<Collaborator> collaborators;
    private final Provider<Organization> organizations;
    private final Provider<Contract> contracts;
    private final EmbeddedStorageManager storageManager;
    public CollaboratorsEntities(@NotNull Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        collaborators = ProviderPersistence.of(storageManager, data.collaborators);
        organizations = ProviderPersistence.of(storageManager, data.organizations);
        contracts = ProviderPersistence.of(storageManager, data.contracts);
    }

    public CollaboratorsEntities() {
        this.data = new Data();
        storageManager = null;
        collaborators = ProviderInMemory.of(data.collaborators);
        organizations = ProviderInMemory.of(data.organizations);
        contracts = ProviderInMemory.of(data.contracts);

    }

    @Override
    public Provider<Collaborator> collaborators() {
        return collaborators;
    }

    @Override
    public Provider<Organization> organizations() {
        return organizations;
    }

    @Override
    public Provider<Contract> contracts() {
        return contracts;
    }

    @Override
    public void close() throws Exception {

    }

    static class Data {
        private final List<Collaborator> collaborators;
        private final List<Organization> organizations;
        private final List<Contract> contracts;

        Data() {
            collaborators = new ArrayList<>();
            organizations = new ArrayList<>();
            contracts = new ArrayList<>();
        }
    }
}
