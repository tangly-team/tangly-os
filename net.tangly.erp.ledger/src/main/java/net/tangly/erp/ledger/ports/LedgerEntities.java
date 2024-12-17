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

package net.tangly.erp.ledger.ports;

import net.tangly.core.domain.Document;
import net.tangly.core.domain.RealmEclipseStore;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderHasId;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerRealm;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LedgerEntities implements LedgerRealm, RealmEclipseStore {
    private static class Data {
        private final List<Account> accounts;
        private final List<Transaction> transactions;
        private final List<Document> documents;

        Data() {
            accounts = new ArrayList<>();
            transactions = new ArrayList<>();
            documents = new ArrayList<>();
        }
    }

    private final Data data;
    private final Provider<Account> accounts;
    private final Provider<Transaction> transactions;
    private final Provider<Document> documents;
    private final EmbeddedStorageManager storageManager;

    public LedgerEntities(@NotNull Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        accounts = ProviderHasId.of(storageManager, data.accounts);
        transactions = ProviderPersistence.of(storageManager, data.transactions);
        documents = ProviderHasId.of(storageManager, data.documents);
    }

    public LedgerEntities() {
        this.data = new Data();
        storageManager = null;
        accounts = ProviderHasId.of(data.accounts);
        transactions = ProviderInMemory.of(data.transactions);
        documents = ProviderHasId.of(data.documents);
    }

    @Override
    public boolean isShutdown() {
        return Objects.nonNull(storageManager) && storageManager.isShutdown();
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
    public Provider<Document> documents() {
        return documents;
    }

    @Override
    public Provider<AccountEntry> entries() {
        return ProviderInMemory.of(accounts().items().stream().flatMap(o -> o.entries().stream()).toList());
    }

    @Override
    public void update(@NotNull Account account) {
        accounts.update(account);
    }

    public void close() {
        if (Objects.nonNull(storageManager)) {
            storageManager.close();
        }
    }
}
