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

package net.tangly.products.ports;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;

import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.Effort;
import net.tangly.bus.products.Product;
import net.tangly.bus.products.ProductsRealm;
import net.tangly.commons.generator.IdGenerator;
import net.tangly.core.HasOid;
import net.tangly.core.app.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

public class ProductsEntities implements ProductsRealm {
    static class Data implements IdGenerator {
        List<Product> products;
        List<Assignment> assignments;
        List<Effort> efforts;
        private long oidCounter;
        private transient final ReentrantLock lock;

        Data() {
            products = new ArrayList<>();
            assignments = new ArrayList<>();
            efforts = new ArrayList<>();
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

    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<Product> products;
    private final Provider<Assignment> assignments;
    private final Provider<Effort> efforts;
    private final EmbeddedStorageManager storageManager;


    public ProductsEntities(Path path) {
        data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        products = new ProviderPersistence<>(storageManager, data.products);
        assignments = new ProviderPersistence<>(storageManager, data.assignments);
        efforts = new ProviderPersistence<>(storageManager, data.efforts);
    }

    @Inject
    public ProductsEntities() {
        data = new Data();
        storageManager = null;
        this.products = new ProviderInMemory<>(data.products);
        this.assignments = new ProviderInMemory<>(data.assignments);
        this.efforts = new ProviderInMemory<>(data.efforts);

        long oidCounter = Realm.maxOid(products());
        oidCounter = Math.max(oidCounter, Realm.maxOid(assignments()));
        oidCounter = Math.max(oidCounter, Realm.maxOid(efforts()));
        data.oidCounter = Math.max(oidCounter, OID_SEQUENCE_START);
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
    public Provider<Product> products() {
        return products;
    }

    @Override
    public Provider<Assignment> assignments() {
        return assignments;
    }

    @Override
    public Provider<Effort> efforts() {
        return efforts;
    }

}
