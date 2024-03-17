/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.invoices.ports;

import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.core.DateRange;
import net.tangly.core.domain.Port;
import net.tangly.erp.invoices.artifacts.*;
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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.tangly.erp.ports.TsvHdl.MODULE;

/**
 * Define the workflow defined for bounded domain activities in particular the import and export of files.
 */
public class InvoicesAdapter implements InvoicesPort {
    public static final String REPORTS = "reports";
    public static final String ARTICLES_TSV = "articles.tsv";
    public static final String JSON_EXT = ".json";
    private final InvoicesRealm realm;

    /**
     * Path to the root folder of all invoices and product description. Invoices should be grouped by year.
     */
    private final Path folder;

    public InvoicesAdapter(@NotNull InvoicesRealm realm, @NotNull Path invoicesFolder) {
        this.realm = realm;
        this.folder = invoicesFolder;
    }

    @Override
    public InvoicesRealm realm() {
        return realm;
    }

    @Override
    public void importEntities() {
        var handler = new InvoicesTsvJsonHdl(realm());
        Port.importEntities(folder, ARTICLES_TSV, handler::importArticles);
        var invoiceJson = new InvoiceJson(realm);
        try (Stream<Path> stream = Files.walk(folder)) {
            AtomicInteger nrOfInvoices = new AtomicInteger();
            AtomicInteger nrOfImportedInvoices = new AtomicInteger();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JSON_EXT)).forEach(o -> {
                nrOfInvoices.getAndIncrement();
                try (Reader reader = Files.newBufferedReader(folder.resolve(o))) {
                    var invoice = handler.importInvoice(reader, o.toString());
                    if ((invoice != null)) {
                        nrOfImportedInvoices.getAndIncrement();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.INFO, "Invoices were imported out of",
                Map.of("nrOfImportedInvoices", Integer.toString(nrOfImportedInvoices.get()), "nrOfInvoices", Integer.toString(nrOfInvoices.get())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportEntities() {
        var handler = new InvoicesTsvJsonHdl(realm());
        handler.exportArticles(folder.resolve(ARTICLES_TSV));
        var invoiceJson = new InvoiceJson(realm);
        realm.invoices().items().forEach(o -> {
            var invoiceFolder = InvoicesUtilities.resolvePath(folder, o);
            var invoicePath = invoiceFolder.resolve(o.name() + JSON_EXT);
            invoiceJson.exports(o, invoicePath, Collections.emptyMap());
            EventData.log(EventData.EXPORT, MODULE, EventData.Status.SUCCESS, "Invoice exported to JSON {}", Map.of("invoice", o, "invoicePath", invoicePath));
        });
    }

    @Override
    public void clearEntities() {
        realm().invoices().deleteAll();
        realm().articles().deleteAll();
    }

    @Override
    public boolean doesInvoiceDocumentExist(@NotNull Invoice invoice) {
        Path invoiceFolder = InvoicesUtilities.resolvePath(folder, invoice);
        Path invoicePdfPath = invoiceFolder.resolve(invoice.name() + AsciiDoctorHelper.PDF_EXT);
        return Files.exists(invoicePdfPath);
    }

    @Override
    public void exportInvoiceDocuments(boolean withQrCode, boolean withEN16931, boolean overwrite, LocalDate from, LocalDate to) {
        final var filter = new DateRange.DateFilter(from, to);
        realm.invoices().items().stream().filter(o -> filter.test(o.date())).forEach(o -> exportInvoiceDocument(o, withQrCode, withEN16931, overwrite));
    }

    @Override
    public void exportInvoiceDocument(@NotNull Invoice invoice, boolean withQrCode, boolean withEN16931, boolean overwrite) {
        var asciiDocGenerator = new InvoiceAsciiDoc(invoice.locale());
        Path invoiceFolder = InvoicesUtilities.resolvePath(folder, invoice);
        Path invoiceAsciiDocPath = invoiceFolder.resolve(invoice.name() + AsciiDoctorHelper.ASCIIDOC_EXT);
        asciiDocGenerator.exports(invoice, invoiceAsciiDocPath, Collections.emptyMap());
        Path invoicePdfPath = invoiceFolder.resolve(invoice.name() + AsciiDoctorHelper.PDF_EXT);
        if (!overwrite && Files.exists(invoicePdfPath)) {
            EventData.log(EventData.EXPORT, "net.tangly.crm.ports", EventData.Status.SUCCESS, "Invoice PDF already exists {}",
                Map.of("invoice", invoice, "invoicePath", invoicePdfPath, "withQrCode", withQrCode, "withEN16931", withEN16931, "overwrite", overwrite));
        } else {
            AsciiDoctorHelper.createPdf(invoiceAsciiDocPath, invoicePdfPath);
            try {
                Files.delete(invoiceAsciiDocPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (withQrCode) {
                var qrGenerator = new InvoiceQrCode();
                qrGenerator.exports(invoice, invoicePdfPath, Collections.emptyMap());
            }
            if (withEN16931) {
                var en164391Generator = new InvoiceZugFerd();
                en164391Generator.exports(invoice, invoicePdfPath, Collections.emptyMap());
            }
            EventData.log(EventData.EXPORT, "net.tangly.crm.ports", EventData.Status.SUCCESS, "Invoice exported to PDF {}",
                Map.of("invoice", invoice, "invoicePath", invoicePdfPath, "withQrCode", withQrCode, "withEN16931", withEN16931, "overwrite", overwrite));
        }
    }
}
