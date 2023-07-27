/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.invoices.ports;

import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.core.DateRange;
import net.tangly.erp.invoices.artifacts.InvoiceAsciiDoc;
import net.tangly.erp.invoices.artifacts.InvoiceQrCode;
import net.tangly.erp.invoices.artifacts.InvoiceZugFerd;
import net.tangly.erp.invoices.artifacts.InvoicesUtilities;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesPort;
import net.tangly.erp.invoices.services.InvoicesRealm;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

/**
 * Invoice adapter is an adapter for the invoice port defined as a secondary port. The port has access to
 * <ul>
 *     <li>realm of the invoices bounded domain and associated entities</li>
 *     <li>folder to the root directory containing all invoice reports and documents</li>
 * </ul>
 */
public class InvoicesAdapter implements InvoicesPort {
    private final InvoicesRealm realm;
    private final Path folder;

    public InvoicesAdapter(InvoicesRealm realm, Path folder) {
        this.realm = realm;
        this.folder = folder;
    }
    @Override
    public void exportInvoiceDocuments(boolean withQrCode, boolean withEN16931, LocalDate from, LocalDate to) {
        final var filter = new DateRange.DateFilter(from, to);
        realm.invoices().items().stream().filter(o -> filter.test(o.date())).forEach(o -> exportInvoiceDocument(o, withQrCode, withEN16931));
    }

    @Override
    public void exportInvoiceDocument(@NotNull Invoice invoice, boolean withQrCode, boolean withEN16931) {
        var asciiDocGenerator = new InvoiceAsciiDoc(invoice.locale());
        Path invoiceFolder = InvoicesUtilities.resolvePath(folder, invoice);
        Path invoiceAsciiDocPath = invoiceFolder.resolve(invoice.name() + AsciiDoctorHelper.ASCIIDOC_EXT);
        asciiDocGenerator.exports(invoice, invoiceAsciiDocPath, Collections.emptyMap());
        Path invoicePdfPath = invoiceFolder.resolve(invoice.name() + AsciiDoctorHelper.PDF_EXT);
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
            Map.of("invoice", invoice, "invoicePath", invoicePdfPath, "withQrCode", withQrCode, "withEN16931", withEN16931));
    }
}
