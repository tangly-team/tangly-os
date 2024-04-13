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

import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.core.domain.Port;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerPort;
import net.tangly.erp.ledger.services.LedgerRealm;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provide workflows for ledger activities.
 * <ul>
 *     <li>Import of the ledger account structure. If using the <a href="https://www.banana.ch">banana</a> application, select all definition rows in
 *     the accounts tab and export it as <i>Data/Export Rows/Export Rows to Txt</i>. Once completed you can use for example MacOs Numbers to remove company
 *     specific information such as segments.</li>
 *     <li>Import transaction journal into the ledger. If using the <a href="https://www.banana.ch">banana</a> application, select all transaction rows in
 *     the accounts tab and export it as <i>Data/Export Rows/Export Rows to Txt</i>. </li>
 * </ul>
 */
public class LedgerAdapter implements LedgerPort {
    public static final String LEDGER = "ledger.tsv";
    public static final String JOURNAL = "-journal.tsv";
    public static final String TURNOVER_ACCOUNT = "3";
    public static final String EBIT_ACCOUNT = "E4";
    public static final String EARNINGS_ACCOUNT = "E7";
    public static final String SHORT_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "20";
    public static final String LONG_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "2A";
    public static final String EQUITY_ACCOUNT = "28";
    public static final String CASH_ON_HAND_ACCOUNT = "100";
    private final LedgerRealm ledger;
    private final Path folder;

    public LedgerAdapter(@NotNull LedgerRealm ledger, @NotNull Path folder) {
        this.ledger = ledger;
        this.folder = folder;
    }

    @Override
    public LedgerRealm realm() {
        return ledger;
    }

    @Override
    public void importEntities() {
        try (Stream<Path> stream = Files.walk(folder)) {
            var handler = new LedgerTsvHdl(ledger);
            Port.importEntities(folder, LEDGER, handler::importChartOfAccounts);
            ledger.build();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JOURNAL)).forEach(o -> {
                try (Reader reader = Files.newBufferedReader(o, StandardCharsets.UTF_8)) {
                    handler.importJournal(reader, o.toString());
                    EventData.log(EventData.IMPORT, LedgerBoundedDomain.DOMAIN, EventData.Status.SUCCESS, "Journal imported {}", Map.of("journalPath", o));
                } catch (IOException e) {
                    EventData.log(EventData.IMPORT, LedgerBoundedDomain.DOMAIN, EventData.Status.FAILURE, "Journal import failed {}", Map.of("journalPath", o));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportEntities() {
        var handler = new LedgerTsvHdl(ledger);
        handler.exportChartOfAccounts(folder.resolve(LEDGER));
        realm().transactions().items().stream().map(Transaction::date).map(LocalDate::getYear).distinct().forEach(o -> {
            Path journal = folder.resolve(journalForYear(o));
            handler.exportJournal(journal, LocalDate.of(o, Month.JANUARY, 1), LocalDate.of(o, Month.DECEMBER, 31));
            EventData.log(EventData.EXPORT, LedgerBoundedDomain.DOMAIN, EventData.Status.SUCCESS, "Journal exported {}", Map.of("journalPath", journal.toString(), "year", o));
        });
    }

    @Override
    public void clearEntities() {
        realm().accounts().deleteAll();
        realm().transactions().deleteAll();
    }

    @Override
    public void exportLedgerDocument(String name, LocalDate from, LocalDate to, boolean withVat, boolean withTransactions) {
        var report = new ClosingReportAsciiDoc(ledger);
        report.create(from, to, folder.resolve(name + AsciiDoctorHelper.ASCIIDOC_EXT), withVat, withTransactions);
        AsciiDoctorHelper.createPdf(folder.resolve(name + AsciiDoctorHelper.ASCIIDOC_EXT), folder.resolve(name + AsciiDoctorHelper.PDF_EXT));
        try {
            Files.delete(folder.resolve(name + AsciiDoctorHelper.ASCIIDOC_EXT));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public static String journalForYear(int year) {
        return year + JOURNAL;
    }
}
