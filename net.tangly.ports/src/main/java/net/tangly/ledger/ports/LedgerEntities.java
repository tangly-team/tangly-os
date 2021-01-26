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

package net.tangly.ledger.ports;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.tangly.bus.ledger.Account;
import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.bus.ledger.Transaction;
import net.tangly.commons.generator.IdGenerator;
import net.tangly.core.HasOid;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

public class LedgerEntities implements LedgerRealm {
    private static class Data implements IdGenerator {
        private final List<Account> accounts;
        private final List<Transaction> transactions;
        private long oidCounter;
        private transient final ReentrantLock lock;

        Data() {
            accounts = new ArrayList<>();
            transactions = new ArrayList<>();
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
    private final Provider<Account> accounts;
    private final Provider<Transaction> transactions;
    private final EmbeddedStorageManager storageManager;


    public LedgerEntities(Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        accounts = new ProviderPersistence<>(storageManager, data.accounts);
        transactions = new ProviderPersistence<>(storageManager, data.transactions);
    }

    public LedgerEntities() {
        this.data = new Data();
        storageManager = null;
        accounts = new ProviderInMemory<>(data.accounts);
        transactions = new ProviderInMemory<>(data.transactions);
    }

    public void storeRoot() {
        if (storageManager != null) {
            storageManager.storeRoot();
        }
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
    public Provider<Account> accounts() {
        return accounts;
    }

    @Override
    public Provider<Transaction> transactions() {
        return transactions;
    }

    @Override
    public void add(@NotNull Transaction transaction) {
        transactions.update(transaction);
    }

    @Override
    public void add(@NotNull Account account) {
        accounts.update(account);

    }

    @Override
    public void close() throws Exception {
        storageManager.close();
    }
}
