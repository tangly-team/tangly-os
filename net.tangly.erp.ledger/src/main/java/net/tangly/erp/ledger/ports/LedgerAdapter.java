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
import net.tangly.core.DateRange;
import net.tangly.core.Tag;
import net.tangly.core.TypeRegistry;
import net.tangly.core.codes.CodeHelper;
import net.tangly.core.domain.Document;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.Operation;
import net.tangly.core.domain.Port;
import net.tangly.core.events.EntityChangedInternalEvent;
import net.tangly.erp.ledger.artifacts.ClosingReportAsciiDoc;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.LedgerTags;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.domain.VatCode;
import net.tangly.erp.ledger.services.LedgerPort;
import net.tangly.erp.ledger.services.LedgerRealm;
import org.eclipse.serializer.exceptions.IORuntimeException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static net.tangly.commons.utilities.AsciiDoctorHelper.PDF_EXT;

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
    private final LedgerRealm realm;
    private final Path dataFolder;
    private final Path docsFolder;
    private TypeRegistry registry;

    public LedgerAdapter(@NotNull LedgerRealm realm, @NotNull Path dataFolder, Path docsFolder) {
        this.realm = realm;
        this.dataFolder = dataFolder;
        this.docsFolder = docsFolder;
    }

    @Override
    public LedgerRealm realm() {
        return realm;
    }

    public TypeRegistry registry() {
        return registry;
    }

    @Override
    public void importEntities(@NotNull DomainAudit audit) {
        var handler = new LedgerTsvHdl(realm, registry);
        handler.importChartOfAccounts(audit, dataFolder.resolve(LEDGER));
        realm.build();
        try (Stream<Path> stream = Files.walk(dataFolder)) {
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JOURNAL)).forEach(o -> {
                try (Reader reader = Files.newBufferedReader(o, StandardCharsets.UTF_8)) {
                    handler.importJournal(audit, reader, o.toString());
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.SUCCESS, "Journal imported {}", Map.of("journalPath", o));
                } catch (IOException e) {
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.FAILURE, "Journal import failed {}", Map.of("journalPath", o));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        entitiesImported(audit);
    }

    @Override
    public void exportEntities(@NotNull DomainAudit audit) {
        var handler = new LedgerTsvHdl(realm, registry);
        handler.exportChartOfAccounts(audit, dataFolder.resolve(LEDGER));
        realm().transactions().items().stream().map(Transaction::date).map(LocalDate::getYear).distinct().forEach(o -> {
            Path journal = dataFolder.resolve(journalForYear(o));
            handler.exportJournal(audit, journal, LocalDate.of(o, Month.JANUARY, 1), LocalDate.of(o, Month.DECEMBER, 31));
            audit.log(EventData.EXPORT_EVENT, EventData.Status.SUCCESS, "Journal exported {}", Map.of("journalPath", journal.toString(), "year", o));
        });
    }

    @Override
    public void clearEntities(@NotNull DomainAudit audit) {
        realm().accounts().deleteAll();
        Port.entitiesCleared(audit, "accounts");
        realm().transactions().deleteAll();
        Port.entitiesCleared(audit, "transactions");
        entitiesImported(audit);
    }

    @Override
    public void importConfiguration(@NotNull DomainAudit audit, @NotNull TypeRegistry registry) {
        this.registry = registry;
        LedgerTags.registerTags(registry);
        try {
            var type = CodeHelper.build(VatCode.class,
                o -> new VatCode(o.getInt("id"), o.getString("code"), o.getBigDecimal("vatRate"), o.getBigDecimal("vatDueRate"), o.getBoolean("enabled")),
                dataFolder.resolve("resources", "VatCodes.json"));
            registry.register(type);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public void exportLedgerDocument(String name, LocalDate from, LocalDate to, boolean withBalanceSheet, boolean withProfitsAndLosses,
                                     boolean withEmptyAccounts, boolean withTransactions, boolean withVat, String text, Collection<Tag> tags,
                                     @NotNull DomainAudit audit) {
        var report = new ClosingReportAsciiDoc(realm, registry);
        report.create(from, to, docsFolder.resolve(name + AsciiDoctorHelper.ASCIIDOC_EXT), withBalanceSheet, withProfitsAndLosses, withEmptyAccounts,
            withTransactions, withVat);
        AsciiDoctorHelper.createPdf(docsFolder.resolve(name + AsciiDoctorHelper.ASCIIDOC_EXT), docsFolder.resolve(name + AsciiDoctorHelper.PDF_EXT), true);
        createDocument(name, from, to, text, tags, audit);
    }

    private void createDocument(@NotNull String name, LocalDate from, LocalDate to, String text, Collection<Tag> tags, @NotNull DomainAudit audit) {
        Document document = new Document(name, name, PDF_EXT, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), new DateRange(from, to), text, true,
            Objects.nonNull(tags) ? tags : Collections.emptyList());
        realm().documents().update(document);
        audit.submitInterally(new EntityChangedInternalEvent(audit.name(), Document.class.getSimpleName(), Operation.CREATE));
    }

    public static String journalForYear(int year) {
        return "%04d%s".formatted(year, JOURNAL);
    }

    private void entitiesImported(@NotNull DomainAudit audit) {
        audit.entityImported(Account.class.getSimpleName());
        audit.entityImported(Transaction.class.getSimpleName());
    }

}
