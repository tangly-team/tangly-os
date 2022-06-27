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
import net.tangly.erp.ledger.ports.ClosingReportAsciiDoc;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.ports.LedgerHdl;
import net.tangly.erp.ledger.ports.LedgerTsvHdl;
import net.tangly.erp.ledger.services.LedgerRealm;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the business logic of the ledger domain model. An in-memory file system is set with a Swiss ledger definition and transactions files.
 */
class LedgerPortTest {
    @Test
    void turnoverEbitAndEarningsTest() throws IOException {
        final String filenameWithoutExtension = "2016-period";
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            var adapter = new LedgerAdapter(createLedger(store), store.ledgerRoot());
            adapter.exportLedgerDocument(filenameWithoutExtension, LocalDate.of(2015, 10, 01), LocalDate.of(2016, 12, 31), true, true);
            assertThat(Files.exists(store.ledgerRoot().resolve(filenameWithoutExtension + ".adoc"))).isFalse();
            assertThat(Files.exists(store.ledgerRoot().resolve(filenameWithoutExtension + ".pdf"))).isTrue();
        }
    }

    @Test
    void testWriteClosingReport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new LedgerTsvHdl(new LedgerEntities());
            var path = store.ledgerRoot().resolve(LedgerHdl.journalForYear(2015));
            handler.importJournal(Files.newBufferedReader(path, StandardCharsets.UTF_8), path.toString());
            path = store.ledgerRoot().resolve(LedgerHdl.journalForYear(2016));
            handler.importJournal(Files.newBufferedReader(path, StandardCharsets.UTF_8), path.toString());

            var report = new ClosingReportAsciiDoc(handler.ledger());
            var writer = new StringWriter();
            report.create(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31), new PrintWriter(writer), true, true);
            assertThat(writer.toString()).isNotEmpty();
        }
    }

    private LedgerRealm createLedger(ErpStore store) {
        store.createRepository();
        var ledgerHdl = new LedgerHdl(new LedgerEntities(), store.ledgerRoot());
        ledgerHdl.importEntities();
        return ledgerHdl.realm();
    }
}
