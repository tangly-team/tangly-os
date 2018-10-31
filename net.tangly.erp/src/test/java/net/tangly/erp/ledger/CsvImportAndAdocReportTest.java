/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.ledger;

import net.tangly.erp.ledger.ports.ClosingReportAsciiDoc;
import net.tangly.erp.ledger.ports.LedgerCsvHdl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvImportAndAdocReportTest {
    @Test
    void testCsvLedgerImport() throws IOException, URISyntaxException {
        LedgerCsvHdl handler = new LedgerCsvHdl(new Ledger());
        Path path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/ledger/swiss-ledger.csv").toURI());
        handler.importStructureLedgerFromBanana8(path);
        handler.ledger().build();
        assertThat(
                handler.ledger().accounts().stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty()).findAny().isEmpty())
                .isTrue();
        assertThat(handler.ledger().assets().isEmpty()).isFalse();
        assertThat(handler.ledger().liabilities().isEmpty()).isFalse();
        assertThat(handler.ledger().profitAndLoss().isEmpty()).isFalse();
        assertThat(handler.ledger().getAccountBy("100").isPresent()).isTrue();
        assertThat(handler.ledger().getAccountBy("1020").isPresent()).isTrue();
        assertThat(handler.ledger().getAccountBy("2A").isPresent()).isTrue();
        assertThat(handler.ledger().getAccountBy("2970").isPresent()).isTrue();
        assertThat(handler.ledger().getAccountsOwnedBy("29A").isEmpty()).isFalse();
    }

    @Test
    void testCsvTransactionsImport() throws IOException, URISyntaxException {
        LedgerCsvHdl handler = new LedgerCsvHdl(new Ledger());
        Path path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/ledger/swiss-ledger.csv").toURI());
        handler.importStructureLedgerFromBanana8(path);
        handler.ledger().build();
        path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/ledger/transactions-2015-2016.csv").toURI());
        handler.importTransactionsLedgerFromBanana8(path);
        assertThat(handler.ledger().transactions(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31)).isEmpty()).isFalse();
        // TODO assert transactions
    }

    @Test
    @Tag("localTest")
    void testCsvImportAndAdocReport() throws IOException {
        LedgerCsvHdl handler = new LedgerCsvHdl(new Ledger());
        handler.importStructureLedgerFromBanana8(Paths.get("/Users/Shared/tmp/swiss-ledger.csv"));
        handler.ledger().build();

        handler.importTransactionsLedgerFromBanana8(Paths.get("/Users/Shared/tmp/period-2016.csv"));
        handler.importTransactionsLedgerFromBanana8(Paths.get("/Users/Shared/tmp/period-2017.csv"));
        handler.importTransactionsLedgerFromBanana8(Paths.get("/Users/Shared/tmp/period-2018.csv"));

        ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(handler.ledger());
        report.create(LocalDate.parse("2015-10-01"), LocalDate.parse("2016-12-31"), Paths.get("/Users/Shared/tmp/closing-2016.adoc"));
        report.create(LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31"), Paths.get("/Users/Shared/tmp/closing-2017.adoc"));
        report.create(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), Paths.get("/Users/Shared/tmp/closing-2018.adoc"));
        report.create(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31"), Paths.get("/Users/Shared/tmp/closing-2019.adoc"));
    }
}
