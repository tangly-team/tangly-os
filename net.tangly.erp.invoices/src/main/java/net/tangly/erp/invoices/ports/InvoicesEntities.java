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

package net.tangly.erp.invoices.ports;

import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import net.tangly.erp.invoices.services.InvoicesRealm;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InvoicesEntities implements InvoicesRealm {
    private static class Data {
        private final List<Invoice> invoices;
        private final List<Article> articles;
        private final List<InvoiceLegalEntity> legalEntities;

        Data() {
            invoices = new ArrayList<>();
            articles = new ArrayList<>();
            legalEntities = new ArrayList<>();
        }
    }

    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<Invoice> invoices;
    private final Provider<Article> articles;
    private final Provider<InvoiceLegalEntity> legalEntities;
    private final EmbeddedStorageManager storageManager;


    public InvoicesEntities(Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        invoices = ProviderPersistence.of(storageManager, data.invoices);
        articles = ProviderPersistence.of(storageManager, data.articles);
        legalEntities = ProviderPersistence.of(storageManager, data.legalEntities);
    }

    public InvoicesEntities() {
        data = new Data();
        storageManager = null;
        invoices = ProviderInMemory.of(data.invoices);
        articles = ProviderInMemory.of(data.articles);
        legalEntities = ProviderInMemory.of(data.legalEntities);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public void shutdown() {
        storageManager.shutdown();
    }

    @Override
    public Provider<Article> articles() {
        return this.articles;
    }

    @Override
    public Provider<Invoice> invoices() {
        return this.invoices;
    }

    @Override
    public Provider<InvoiceLegalEntity> legalEntities() {
        return legalEntities;
    }

    @Override
    public void close() throws Exception {
        storageManager.close();
    }
}
