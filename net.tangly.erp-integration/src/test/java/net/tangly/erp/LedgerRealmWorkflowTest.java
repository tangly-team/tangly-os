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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.time.LocalDate;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.ledger.Account;
import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.ledger.ports.ClosingReportAsciiDoc;
import net.tangly.ledger.ports.LedgerTsvHdl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerRealmWorkflowTest {
    private static final String PACKAGE_NAME = "net/tangly/crm/ledger/";
    private static final String SWISS_LEDGER = "swiss-ledger.tsv";

    @Test
    void testCsvLedgerImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore erpStore = new ErpStore(fs);
            erpStore.createCrmAndLedgerRepository();

            LedgerTsvHdl handler = new LedgerTsvHdl(new LedgerRealm());
            handler.importLedgerStructureFromBanana(erpStore.ledgerRoot().resolve(SWISS_LEDGER));
            handler.ledger().build();
            assertThat(handler.ledger().accounts().stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty()).findAny().isEmpty())
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
    void testCsvTransactionsImport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore erpStore = new ErpStore(fs);
            erpStore.createCrmAndLedgerRepository();

            LedgerTsvHdl handler = new LedgerTsvHdl(new LedgerRealm());
            handler.importTransactionsLedgerFromBanana(erpStore.ledgerRoot().resolve("transactions-2015-2016.tsv"));
            assertThat(handler.ledger().transactions(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31)).isEmpty()).isFalse();
        }
    }

    @Test
    void testWriteClosingReport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore erpStore = new ErpStore(fs);
            erpStore.createCrmAndLedgerRepository();

            LedgerTsvHdl handler = new LedgerTsvHdl(new LedgerRealm());
            handler.importTransactionsLedgerFromBanana(erpStore.ledgerRoot().resolve("transactions-2015-2016.tsv"));

            ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(handler.ledger());
            StringWriter writer = new StringWriter();
            report.create(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31), new PrintWriter(writer), true, true);
            assertThat(writer.toString().isEmpty()).isFalse();
        }
    }
}

