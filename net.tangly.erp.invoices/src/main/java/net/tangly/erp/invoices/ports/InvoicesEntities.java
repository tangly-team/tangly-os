/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.erp.invoices.ports;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.tangly.commons.generator.IdGenerator;
import net.tangly.core.HasOid;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import net.tangly.erp.invoices.services.InvoicesRealm;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

public class InvoicesEntities implements InvoicesRealm {
    private static class Data implements IdGenerator {
        private final List<Invoice> invoices;
        private final List<Article> articles;
        private final List<InvoiceLegalEntity> legalEntities;
        private long oidCounter;
        private transient final ReentrantLock lock;

        Data() {
            invoices = new ArrayList<>();
            articles = new ArrayList<>();
            legalEntities = new ArrayList<>();
            oidCounter = HasOid.UNDEFINED_OID;
            this.lock = new ReentrantLock();
        }

        @Override
        public long id() {
            lock.lock();
            try {
                return oidCounter++;
            } finally {
                lock.unlock();
            }
        }

    }

    private final Data data;
    private final Provider<Invoice> invoices;
    private final Provider<Article> articles;
    private final Provider<InvoiceLegalEntity> legalEntities;
    private final EmbeddedStorageManager storageManager;


    public InvoicesEntities(Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        invoices = new ProviderPersistence<>(storageManager, data.invoices);
        articles = new ProviderPersistence<>(storageManager, data.articles);
        legalEntities = new ProviderPersistence<>(storageManager, data.legalEntities);
    }

    public InvoicesEntities() {
        data = new Data();
        storageManager = null;
        invoices = new ProviderInMemory<>(data.invoices);
        articles = new ProviderInMemory<>(data.articles);
        legalEntities = new ProviderInMemory<>(data.legalEntities);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public void shutdown() {
        storageManager.shutdown();
    }

    @Override
    public <T extends HasOid> T registerOid(@NotNull T entity) {
        Realm.setOid(entity, data.id());
        storeRoot();
        return entity;
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
