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

package net.tangly.erp.invoices.ports;

import net.tangly.core.domain.Document;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import net.tangly.erp.invoices.services.InvoicesRealm;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InvoicesEntities implements InvoicesRealm {
    private static class Data {
        private final List<Invoice> invoices;
        private final List<Article> articles;
        private final List<InvoiceLegalEntity> legalEntities;
        private final List<Document> documents;

        Data() {
            invoices = new ArrayList<>();
            articles = new ArrayList<>();
            legalEntities = new ArrayList<>();
            documents = new ArrayList<>();
        }
    }

    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<Invoice> invoices;
    private final Provider<Article> articles;
    private final Provider<InvoiceLegalEntity> legalEntities;
    private final Provider<Document> documents;
    private final EmbeddedStorageManager storageManager;


    public InvoicesEntities(Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        invoices = ProviderPersistence.of(storageManager, data.invoices);
        articles = ProviderPersistence.of(storageManager, data.articles);
        legalEntities = ProviderPersistence.of(storageManager, data.legalEntities);
        documents = ProviderPersistence.of(storageManager, data.documents);
    }

    public InvoicesEntities() {
        data = new Data();
        storageManager = null;
        invoices = ProviderInMemory.of(data.invoices);
        articles = ProviderInMemory.of(data.articles);
        legalEntities = ProviderInMemory.of(data.legalEntities);
        documents = ProviderInMemory.of(data.documents);
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
    public Provider<Document> documents() {
        return this.documents;
    }

    public void close() {
        if (Objects.nonNull(storageManager)) {
            storageManager.close();
        }
    }
}
