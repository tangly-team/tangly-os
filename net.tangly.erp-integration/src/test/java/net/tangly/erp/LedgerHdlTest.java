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

package net.tangly.erp;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.LocalDate;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.ledger.Account;
import net.tangly.ledger.ports.LedgerEntities;
import net.tangly.ledger.ports.LedgerTsvHdl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerHdlTest {
    private static final String PACKAGE_NAME = "net/tangly/crm/ledger/";
    private static final String SWISS_LEDGER = "swiss-ledger.tsv";

    @Test
    void testTsvLedgerImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore erpStore = new ErpStore(fs);
            erpStore.createCrmAndLedgerRepository();

            LedgerTsvHdl handler = new LedgerTsvHdl(new LedgerEntities());
            handler.importChartOfAccounts(erpStore.ledgerRoot().resolve(SWISS_LEDGER));
            handler.ledger().build();
            assertThat(
                handler.ledger().accounts().items().stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty()).findAny().isEmpty())
                .isTrue();
            assertThat(handler.ledger().assets().isEmpty()).isFalse();
            assertThat(handler.ledger().liabilities().isEmpty()).isFalse();
            assertThat(handler.ledger().profitAndLoss().isEmpty()).isFalse();
            assertThat(handler.ledger().accountBy("100").isPresent()).isTrue();
            assertThat(handler.ledger().accountBy("1020").isPresent()).isTrue();
            assertThat(handler.ledger().accountBy("2A").isPresent()).isTrue();
            assertThat(handler.ledger().accountBy("2970").isPresent()).isTrue();
            assertThat(handler.ledger().accountsOwnedBy("29A").isEmpty()).isFalse();
        }
    }

    @Test
    void testTsvLedgerImportExport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore erpStore = new ErpStore(fs);
            erpStore.createCrmAndLedgerRepository();

            LedgerTsvHdl handler = new LedgerTsvHdl(new LedgerEntities());
            handler.importChartOfAccounts(erpStore.ledgerRoot().resolve(SWISS_LEDGER));
            handler.ledger().build();
            int nrOfAccounts = handler.ledger().accounts().items().size();
            int nrOfBookableAccounts = handler.ledger().bookableAccounts().size();
            int nrOfLiabilitiesAccounts = handler.ledger().liabilities().size();
            int nrOfProfitAndLossAccounts = handler.ledger().profitAndLoss().size();

            handler.exportChartOfAccounts(erpStore.ledgerRoot().resolve(SWISS_LEDGER));
            handler = new LedgerTsvHdl(new LedgerEntities());
            handler.importChartOfAccounts(erpStore.ledgerRoot().resolve(SWISS_LEDGER));
            handler.ledger().build();
            assertThat(handler.ledger().accounts().items().size()).isEqualTo(nrOfAccounts);
            assertThat(handler.ledger().bookableAccounts().size()).isEqualTo(nrOfBookableAccounts);
            assertThat(handler.ledger().liabilities().size()).isEqualTo(nrOfLiabilitiesAccounts);
            assertThat(handler.ledger().profitAndLoss().size()).isEqualTo(nrOfProfitAndLossAccounts);
        }
    }

    @Test
    void testTsvTransactionsImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore erpStore = new ErpStore(fs);
            erpStore.createCrmAndLedgerRepository();

            LedgerTsvHdl handler = new LedgerTsvHdl(new LedgerEntities());
            handler.importChartOfAccounts(erpStore.ledgerRoot().resolve(SWISS_LEDGER));
            handler.ledger().build();

            handler.importJournal(erpStore.ledgerRoot().resolve("transactions-2015-2016.tsv"));
            assertThat(handler.ledger().transactions(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31)).isEmpty()).isFalse();
        }
    }

    @Test
    void testTsvTransactionsImportExport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore erpStore = new ErpStore(fs);
            erpStore.createCrmAndLedgerRepository();

            LedgerTsvHdl handler = new LedgerTsvHdl(new LedgerEntities());
            handler.importChartOfAccounts(erpStore.ledgerRoot().resolve(SWISS_LEDGER));
            handler.ledger().build();
            handler.importJournal(erpStore.ledgerRoot().resolve("transactions-2015-2016.tsv"));

            handler.exportJournal(Path.of("/Users/Shared/tmp/foo.tsv"), null, null);
        }
    }
}

