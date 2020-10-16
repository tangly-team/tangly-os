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

package net.tangly.crm.ports;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.ledger.Ledger;
import net.tangly.ledger.ports.LedgerBusinessLogic;
import net.tangly.ledger.ports.LedgerHdl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the business logic of the ledger domain model. An in-memory file system is set with a Swiss ledger definition and transactions files.
 */
class LedgerBusinessLogicTest {

    @Test
    @Tag("localTest")
    public void createReports() {
        LedgerHdl ledgerHdl = new LedgerHdl(new Ledger());
        ledgerHdl.importEntities(Paths.get("/Users/Shared/tangly/"));
        LedgerBusinessLogic ledgerLogic = new LedgerBusinessLogic(ledgerHdl.ledger());

        Path report = Paths.get("/Users/Shared/tangly/reports/ledger");
        ledgerLogic.createLedgerReport(report, "tangly-" + 2016, LocalDate.of(2015, 11, 1), LocalDate.of(2016, 12, 31));
        List.of(2017, 2018, 2019, 2020).forEach(o -> ledgerLogic.createLedgerReport(report, "tangly-" + o, LocalDate.of(o, 1, 1), LocalDate.of(o, 12, 31)));
    }

    @Test
    public void turnoverEbitAndEarningsTest() throws IOException {
        final String filenameWithoutExtension = "2016-period";
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            CrmAndLedgerStore store = new CrmAndLedgerStore(fs);
            LedgerBusinessLogic logic = new LedgerBusinessLogic(createLedger(store));
            logic.createLedgerReport(store.ledgerRoot(), filenameWithoutExtension, LocalDate.of(2015, 10, 01), LocalDate.of(2016, 12, 31));
            assertThat(Files.exists(store.ledgerRoot().resolve(filenameWithoutExtension + ".adoc"))).isFalse();
            assertThat(Files.exists(store.ledgerRoot().resolve(filenameWithoutExtension + ".pdf"))).isTrue();
        }
    }

    private Ledger createLedger(CrmAndLedgerStore store) {
        store.createCrmAndLedgerRepository();
        LedgerHdl ledgerHdl = new LedgerHdl(new Ledger());
        ledgerHdl.importEntities(store.ledgerRoot());
        return ledgerHdl.ledger();
    }
}
