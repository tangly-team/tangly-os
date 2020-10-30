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

package net.tangly.invoices.ports;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import org.jetbrains.annotations.NotNull;

public class InvoicesPort {
    private final RealmInvoices realm;
    private final Path folder;

    public InvoicesPort(RealmInvoices realm, Path folder) {
        this.realm = realm;
        this.folder = folder;
    }

    public void exportInvoiceDocuments(Path directory, boolean withQrCode, boolean withEN16931) {
        realm.invoices().items().forEach(o -> exportInvoiceDocument(o, directory, withQrCode, withEN16931));
    }

    /**
     * Export an invoice to a file.
     *
     * @param invoice       invoice to be exported
     * @param invoiceFolder path of the file where the invoice will exported
     * @param withQrCode    flag if the Swiss QR cde should be added to the invoice document
     * @param withEN16931   flag if the EN16931 digital invoice should be added to the invoice document
     */
    public void exportInvoiceDocument(@NotNull Invoice invoice, @NotNull Path invoiceFolder, boolean withQrCode, boolean withEN16931) {
        InvoiceAsciiDoc asciiDocGenerator = new InvoiceAsciiDoc(invoice.locale());
        asciiDocGenerator.exports(invoice, invoiceFolder, Collections.emptyMap());
        Path invoicePath = invoiceFolder.resolve(invoice.name() + AsciiDoctorHelper.PDF_EXT);
        if (withQrCode) {
            InvoiceQrCode qrGenerator = new InvoiceQrCode();
            qrGenerator.exports(invoice, invoicePath, Collections.emptyMap());
        }
        if (withEN16931) {
            InvoiceZugFerd en164391Generator = new InvoiceZugFerd();
            en164391Generator.exports(invoice, invoicePath, Collections.emptyMap());
        }
        EventData.log(EventData.EXPORT, "net.tangly.crm.ports", EventData.Status.SUCCESS, "Invoice exported to PDF {}",
                Map.of("invoice", invoice, "invoicePath", invoicePath, "withQrCode", withQrCode, "withEN16931", withEN16931));
    }
}
