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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import net.tangly.bus.ledger.Account;
import net.tangly.bus.ledger.Ledger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvImportAndAdocReportTest {
    @Test
    void testCsvLedgerImport() throws URISyntaxException {
        LedgerCsvHdl handler = new LedgerCsvHdl(new Ledger());
        Path path = Paths.get(getClass().getClassLoader().getResource("net/tangly/ledger/ports/swiss-ledger.csv").toURI());
        handler.importLedgerStructureFromBanana(path);
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

    @Test
    void testCsvTransactionsImport() throws IOException, URISyntaxException {
        LedgerCsvHdl handler = createLedger();
        Path path = Paths.get(getClass().getClassLoader().getResource("net/tangly/ledger/ports/transactions-2015-2016.csv").toURI());
        handler.importTransactionsLedgerFromBanana(path);
        assertThat(handler.ledger().transactions(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31)).isEmpty()).isFalse();
        ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(handler.ledger());
        StringWriter writer = new StringWriter();
        report.create(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31), new PrintWriter(writer));
        assertThat(writer.toString().isEmpty()).isFalse();
    }

    @Test
    @Tag("localTest")
    void testCsvImportAndAdocReport() {
        LedgerWorkflows workflows = new LedgerWorkflows();

        workflows.importLedger(Paths.get("/Users/Shared/tangly"));

        ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(workflows.ledger());
        report.create(LocalDate.parse("2015-10-01"), LocalDate.parse("2016-12-31"), Paths.get("/tmp/closing-2016.adoc"));
        report.create(LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31"), Paths.get("/tmp/closing-2017.adoc"));
        report.create(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), Paths.get("/tmp/closing-2018.adoc"));
        // report.create(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31"), Paths.get("/tmp/closing-2019.adoc"));

        assertThat(Files.exists(Paths.get("/tmp/closing-2016.adoc"))).isTrue();
        assertThat(Files.exists(Paths.get("/tmp/closing-2017.adoc"))).isTrue();
        assertThat(Files.exists(Paths.get("/tmp/closing-2018.adoc"))).isTrue();
        // assertThat(Files.exists(Paths.get("/tmp/closing-2019.adoc"))).isTrue();
    }

    private LedgerCsvHdl createLedger() throws URISyntaxException {
        LedgerCsvHdl handler = new LedgerCsvHdl(new Ledger());
        Path path = Paths.get(getClass().getClassLoader().getResource("net/tangly/ledger/ports/swiss-ledger.csv").toURI());
        handler.importLedgerStructureFromBanana(path);
        handler.ledger().build();
        return handler;
    }
}

