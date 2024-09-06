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

package net.tangly.erp.invoices.ports;

import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.core.DateRange;
import net.tangly.core.Tag;
import net.tangly.core.domain.*;
import net.tangly.core.events.EntityChangedInternalEvent;
import net.tangly.core.providers.Provider;
import net.tangly.erp.invoices.artifacts.InvoiceAsciiDoc;
import net.tangly.erp.invoices.artifacts.InvoiceJson;
import net.tangly.erp.invoices.artifacts.InvoiceQrCode;
import net.tangly.erp.invoices.artifacts.InvoiceZugFerd;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesPort;
import net.tangly.erp.invoices.services.InvoicesRealm;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.tangly.commons.utilities.AsciiDoctorHelper.PDF_EXT;
import static net.tangly.core.domain.TsvHdl.DOCUMENTS_TSV;

/**
 * Define the workflow defined for bounded domain activities, in particular the import and export of files.
 */
public class InvoicesAdapter implements InvoicesPort {
    private final InvoicesRealm realm;

    /**
     * Path to the root folder of all invoices and product description. Invoices should be grouped by year.
     */
    private final Path dataFolder;
    private final Path docsFolder;
    private final Properties properties;

    public InvoicesAdapter(@NotNull InvoicesRealm realm, @NotNull Path dataFolder, @NotNull Path docsFolder, @NotNull Properties properties) {
        this.realm = realm;
        this.dataFolder = dataFolder;
        this.docsFolder = docsFolder;
        this.properties = properties;
    }

    @Override
    public InvoicesRealm realm() {
        return realm;
    }

    @Override
    public void importEntities(@NotNull DomainAudit audit) {
        var handler = new InvoicesTsvJsonHdl(realm());
        handler.importArticles(audit, dataFolder.resolve(ARTICLES_TSV));
        try (Stream<Path> stream = Files.walk(dataFolder)) {
            var nrOfInvoices = new AtomicInteger();
            var nrOfImportedInvoices = new AtomicInteger();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JSON_EXT)).forEach(o -> {
                nrOfInvoices.getAndIncrement();
                try (Reader reader = Files.newBufferedReader(dataFolder.resolve(o))) {
                    var invoice = handler.importInvoice(audit, reader, o.toString());
                    if ((invoice != null)) {
                        nrOfImportedInvoices.getAndIncrement();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, "Invoices were imported out of",
                Map.of("nrOfImportedInvoices", Integer.toString(nrOfImportedInvoices.get()), "rootFolder", dataFolder.toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        TsvHdl.importDocuments(audit, dataFolder.resolve(DOCUMENTS_TSV), realm().documents());
        entitiesImported(audit);
    }

    @Override
    public void exportEntities(@NotNull DomainAudit audit) {
        var handler = new InvoicesTsvJsonHdl(realm());
        handler.exportArticles(audit, dataFolder.resolve(ARTICLES_TSV));
        var invoiceJson = new InvoiceJson(realm());
        realm().invoices().items().forEach(o -> {
            var invoicePath = Port.resolvePath(dataFolder, o.date().getYear(), o.name() + JSON_EXT);
            invoiceJson.export(o, true, invoicePath, audit);
            audit.log(EventData.EXPORT_EVENT, EventData.Status.SUCCESS, "Invoice exported to JSON {}", Map.of(INVOICE, o, INVOICE_PATH, invoicePath));
        });
        TsvHdl.exportDocuments(audit, dataFolder.resolve(DOCUMENTS_TSV), realm().documents());
    }

    @Override
    public void clearEntities(@NotNull DomainAudit audit) {
        realm().invoices().deleteAll();
        Port.entitiesCleared(audit, "invoices");
        realm().articles().deleteAll();
        Port.entitiesCleared(audit, "articles");
        entitiesImported(audit);
    }

    @Override
    public void exportInvoiceDocuments(@NotNull DomainAudit audit, boolean withQrCode, boolean withEN16931, boolean overwrite, LocalDate from, LocalDate to,
                                       Collection<Tag> tags) {
        final var filter = new DateRange.DateFilter(from, to);
        realm().invoices().items().stream().filter(o -> filter.test(o.date()))
            .forEach(o -> exportInvoiceDocument(audit, o, withQrCode, withEN16931, overwrite, tags));
    }

    @Override
    public Optional<InvoiceView> invoiceViewFor(@NotNull String id) {
        return Provider.findById(realm().invoices(), id).stream()
            .flatMap(o -> Optional.of(new InvoiceView(o.id(), o.currency(), o.amountWithoutVat(), o.vat(), o.amountWithVat(), o.date(), o.dueDate())).stream())
            .findAny();
    }

    @Override
    public void exportInvoiceDocument(@NotNull DomainAudit audit, @NotNull Invoice invoice, boolean withQrCode, boolean withEN16931, boolean overwrite,
                                      Collection<Tag> tags) {
        var invoiceAsciiDocPath = Port.resolvePath(docsFolder, invoice.date().getYear(), invoice.name() + AsciiDoctorHelper.ASCIIDOC_EXT);
        Path invoicePdfPath = Port.resolvePath(docsFolder, invoice.date().getYear(), invoice.name() + PDF_EXT);
        var asciiDocGenerator = new InvoiceAsciiDoc(invoice.locale(), properties);
        asciiDocGenerator.export(invoice, overwrite, invoiceAsciiDocPath, audit);
        if (!overwrite && Files.exists(invoicePdfPath)) {
            audit.log(EventData.EXPORT_EVENT, EventData.Status.SUCCESS, "Invoice PDF already exists {}",
                Map.of(INVOICE, invoice, INVOICE_PATH, invoicePdfPath, "withQrCode", withQrCode, "withEN16931", withEN16931, "overwrite", overwrite));
        } else {
            AsciiDoctorHelper.createPdf(invoiceAsciiDocPath, invoicePdfPath, true);
            if (withQrCode) {
                var qrGenerator = new InvoiceQrCode();
                qrGenerator.export(invoice, true, invoicePdfPath, audit);
            }
            if (withEN16931) {
                var en164391Generator = new InvoiceZugFerd();
                en164391Generator.export(invoice, true, invoicePdfPath, audit);
            }
            audit.log(EventData.EXPORT_EVENT, EventData.Status.SUCCESS, "Invoice exported to PDF {}",
                Map.of(INVOICE, invoice, INVOICE_PATH, invoicePdfPath, "withQrCode", withQrCode, "withEN16931", withEN16931, "overwrite", overwrite));
        }
        createDocument(invoice, tags, audit);
    }

    private void createDocument(@NotNull Invoice invoice, Collection<Tag> tags, @NotNull DomainAudit audit) {
        String id = Path.of(Integer.toString(invoice.date().getYear()), invoice.name()).toString();
        Document document =
            new Document(id, invoice.name(), PDF_EXT, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), new DateRange(invoice.date(), invoice.date()),
                invoice.text(), true, Objects.nonNull(tags) ? tags : Collections.emptyList());
        realm().documents().update(document);
        audit.submitInterally(new EntityChangedInternalEvent(audit.name(), Document.class.getSimpleName(), Operation.CREATE));
    }

    private void entitiesImported(@NotNull DomainAudit audit) {
        audit.entityImported(Invoice.class.getSimpleName());
        audit.entityImported(Article.class.getSimpleName());
    }
}
