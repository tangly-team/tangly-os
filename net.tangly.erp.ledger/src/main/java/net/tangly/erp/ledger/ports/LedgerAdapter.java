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
import net.tangly.core.domain.Port;
import net.tangly.core.domain.TsvHdl;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.ledger.artifacts.ClosingReportAsciiDoc;
import net.tangly.erp.ledger.domain.*;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerPort;
import net.tangly.erp.ledger.services.LedgerRealm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.serializer.exceptions.IORuntimeException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static net.tangly.commons.utilities.AsciiDoctorHelper.PDF_EXT;
import static net.tangly.core.domain.TsvHdl.DOCUMENTS_TSV;

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
    private static final Logger logger = LogManager.getLogger();
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
        TsvHdl.importDocuments(audit, dataFolder.resolve(DOCUMENTS_TSV), realm().documents());
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
        TsvHdl.exportDocuments(audit, dataFolder.resolve(DOCUMENTS_TSV), realm().documents());
    }

    @Override
    public void clearEntities(@NotNull DomainAudit audit) {
        realm().accounts().deleteAll();
        Port.entitiesCleared(audit, "accounts");
        realm().transactions().deleteAll();
        Port.entitiesCleared(audit, "transactions");
        entitiesImported(audit);
        realm().documents().deleteAll();
        Port.entitiesCleared(audit, "documents");
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
            audit.log(EventData.IMPORT_EVENT, EventData.Status.SUCCESS, "Vat codes imported",
                Map.of("code", VatCode.class.getSimpleName(), "vatCodes", type.toString()));
        } catch (IOException e) {
            audit.log(EventData.IMPORT_EVENT, EventData.Status.FAILURE, "Vat codes not imported",
                Map.of("code", VatCode.class.getSimpleName(), "exception", e));
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

    public void populateExpectedDates(@NotNull LedgerBoundedDomain domain) {
        InvoicesBoundedDomain invoicesBoundedDomain = (InvoicesBoundedDomain) domain.directory().getBoundedDomain(InvoicesBoundedDomain.DOMAIN).orElseThrow();
        realm().transactions().items().stream().filter(o -> o.dateExpected() == null).toList().forEach(
            o -> invoicesBoundedDomain.port().invoiceViewFor(o.reference()).ifPresent(p -> realm().transactions().replace(o, o.withDateExpected(p.dueDate()))));
    }

    @Override
    public Collection<Segment> computeSegments(@NotNull String code, LocalDate from, LocalDate to) {
        var segments = new HashMap<String, Segment>();
        try {
            realm().transactions(from, to).stream().flatMap(o -> o.creditSplits().stream()).filter(o -> o.containsTag(AccountEntry.FINANCE, code))
                .forEach(o -> update(segments, o.value(AccountEntry.FINANCE, code).orElseThrow(), o));
        } catch (Exception e) {
            // TODO solve problem with EclipseStore
            logger.atError().withThrowable(e).log("Error computing segments for code (EclipseStore known problem {}", code);
        }
        return segments.values();
    }

    private void update(@NotNull Map<String, Segment> segments, String key, @NotNull AccountEntry entry) {
        BigDecimal amount = entry.isCredit() ? entry.amount().subtract(entry.getVatDue()) : entry.amount().negate();
        if (segments.containsKey(key)) {
            segments.compute(key, (k, segment) -> new Segment(key, entry.date().isBefore(segment.from()) ? entry.date() : segment.from(),
                entry.date().isAfter(segment.to()) ? entry.date() : segment.to(), segment.amount().add(amount)));
        } else {
            segments.put(key, new Segment(key, entry.date(), entry.date(), amount));
        }
    }

    private void createDocument(@NotNull String name, LocalDate from, LocalDate to, String text, Collection<Tag> tags, @NotNull DomainAudit audit) {
        Document document = new Document(name, name, PDF_EXT, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), new DateRange(from, to), text, true,
            Objects.nonNull(tags) ? tags : Collections.emptyList());
        realm().documents().update(document);
        Document.update(realm.documents(), document, audit);
    }

    public static String journalForYear(int year) {
        return "%04d%s".formatted(year, JOURNAL);
    }

    private void entitiesImported(@NotNull DomainAudit audit) {
        audit.entityImported(Account.class.getSimpleName());
        audit.entityImported(Transaction.class.getSimpleName());
    }

}
