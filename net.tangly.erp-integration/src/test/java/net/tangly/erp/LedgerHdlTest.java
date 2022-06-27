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

package net.tangly.erp;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.ports.LedgerHdl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerHdlTest {
    @Test
    void testTsvLedgerImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new LedgerHdl(new LedgerEntities(), store.ledgerRoot());
            handler.importEntities();
            assertThat(handler.realm().accounts().items().stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty()).findAny())
                .isEmpty();
            assertThat(handler.realm().assets().isEmpty()).isFalse();
            assertThat(handler.realm().liabilities()).isNotEmpty();
            assertThat(handler.realm().profitAndLoss()).isNotEmpty();
            assertThat(handler.realm().accountBy("100")).isPresent();
            assertThat(handler.realm().accountBy("1020")).isPresent();
            assertThat(handler.realm().accountBy("2A")).isPresent();
            assertThat(handler.realm().accountBy("2970")).isPresent();
            assertThat(handler.realm().accountsOwnedBy("29A")).isNotEmpty();
        }
    }

    @Test
    void testTsvLedgerImportExport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new LedgerHdl(new LedgerEntities(), store.ledgerRoot());
            handler.importEntities();
            int nrOfAccounts = handler.realm().accounts().items().size();
            int nrOfBookableAccounts = handler.realm().bookableAccounts().size();
            int nrOfLiabilitiesAccounts = handler.realm().liabilities().size();
            int nrOfProfitAndLossAccounts = handler.realm().profitAndLoss().size();

            handler.exportEntities();
            handler = new LedgerHdl(new LedgerEntities(), store.ledgerRoot());
            handler.importEntities();
            assertThat(handler.realm().accounts().items().size()).isEqualTo(nrOfAccounts);
            assertThat(handler.realm().bookableAccounts().size()).isEqualTo(nrOfBookableAccounts);
            assertThat(handler.realm().liabilities().size()).isEqualTo(nrOfLiabilitiesAccounts);
            assertThat(handler.realm().profitAndLoss().size()).isEqualTo(nrOfProfitAndLossAccounts);
        }
    }

    @Test
    void testTsvTransactionsImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var handler = new LedgerHdl(new LedgerEntities(), store.ledgerRoot());
            handler.importEntities();
            assertThat(handler.realm().transactions(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31)).isEmpty()).isFalse();
        }
    }

    @Test
    void testTsvTransactionsImportExport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var handler = new LedgerHdl(new LedgerEntities(), store.ledgerRoot());
            handler.importEntities();
            int nrOfTransactions = handler.realm().transactions().items().size();

            handler.exportEntities();

            handler = new LedgerHdl(new LedgerEntities(), store.ledgerRoot());
            handler.importEntities();
            assertThat(handler.realm().transactions().items().size()).isEqualTo(nrOfTransactions);
        }
    }
}
