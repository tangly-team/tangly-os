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

package net.tangly.erp;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerPort;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class LedgerHdlTest {
    @Test
    void testTsvLedgerImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var handler = createLedgerPort(store);
            handler.importEntities(store);
            assertThat(
                handler.realm().accounts().items().stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty()).findAny()).isEmpty();
            assertThat(handler.realm().assets()).isNotEmpty();
            assertThat(handler.realm().liabilities()).isNotEmpty();
            assertThat(handler.realm().profitAndLoss()).isNotEmpty();
            assertThat(handler.realm().accountBy("100")).isPresent();
            assertThat(handler.realm().accountBy("1020")).isPresent();
            assertThat(handler.realm().accountBy("2A")).isPresent();
            assertThat(handler.realm().accountBy("2970")).isPresent();
            assertThat(handler.realm().accountsOwnedBy("29A")).isNotEmpty();
            closeLedgerPort(handler);
        }
    }

    @Test
    @Disabled
    void testTsvLedgerImportExport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var handler = createLedgerPort(store);
            handler.importEntities(store);
            int nrOfAccounts = handler.realm().accounts().items().size();
            int nrOfBookableAccounts = handler.realm().bookableAccounts().size();
            int nrOfLiabilitiesAccounts = handler.realm().liabilities().size();
            int nrOfProfitAndLossAccounts = handler.realm().profitAndLoss().size();
            int nrOfTransactions = handler.realm().transactions().items().size();

            handler.exportEntities(store);
            closeLedgerPort(handler);

            handler = createLedgerPort(store);
            handler.importEntities(store);
            assertThat(handler.realm().accounts().items()).hasSize(nrOfAccounts);
            assertThat(handler.realm().bookableAccounts()).hasSize(nrOfBookableAccounts);
            assertThat(handler.realm().liabilities()).hasSize(nrOfLiabilitiesAccounts);
            assertThat(handler.realm().profitAndLoss()).hasSize(nrOfProfitAndLossAccounts);
            assertThat(handler.realm().transactions().items()).hasSize(nrOfTransactions);
            closeLedgerPort(handler);
        }
    }

    @Test
    void testTsvTransactionsImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var handler = createLedgerPort(store);
            handler.importEntities(store);
            assertThat(handler.realm().transactions(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31))).isNotEmpty();
            closeLedgerPort(handler);
        }
    }

    @Test
    @Disabled
    void testTsvTransactionsImportExport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var handler = createLedgerPort(store);
            handler.importEntities(store);
            int nrOfTransactions = handler.realm().transactions().items().size();

            handler.exportEntities(store);
            closeLedgerPort(handler);

            handler = createLedgerPort(store);
            handler.importEntities(store);
            assertThat(handler.realm().transactions().items()).hasSize(nrOfTransactions);
            closeLedgerPort(handler);
        }
    }

    private void closeLedgerPort(@NotNull LedgerPort handler) {
        handler.realm().close();
        await().until(() -> ((LedgerEntities) handler.realm()).isShutdown());
    }

    private LedgerPort createLedgerPort(@NotNull ErpStore store) {
        Path ledgerDb = store.dbRoot().resolve(LedgerBoundedDomain.DOMAIN);
        Path ledgerData = store.dataRoot().resolve(LedgerBoundedDomain.DOMAIN);
        Path ledgerReport = store.docsRoot().resolve(LedgerBoundedDomain.DOMAIN);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var handler = new LedgerAdapter(new LedgerEntities(ledgerDb), ledgerData, ledgerReport);
        handler.importConfiguration(store, new TypeRegistry());
        return handler;
    }
}
