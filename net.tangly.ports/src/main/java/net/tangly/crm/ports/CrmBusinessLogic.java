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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;

import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.commons.utilities.DateUtilities;
import net.tangly.invoices.ports.InvoiceAsciiDoc;
import net.tangly.invoices.ports.InvoiceQrCode;
import net.tangly.invoices.ports.InvoiceZugFerd;
import org.jetbrains.annotations.NotNull;

/**
 * Define business logic rules and functions for the CRM domain model. It connects the CRM entities with the invoices component.
 */
public class CrmBusinessLogic {
    private final Crm crm;

    public CrmBusinessLogic(@NotNull Crm crm) {
        this.crm = crm;
    }

    /**
     * Export an invoice to a file.
     *
     * @param invoice     invoice to be exported
     * @param invoiceFolder path of the file where the invoice will exported
     * @param withQrCode  flag if the Swiss QR cde should be added to the invoice document
     * @param withEN16931 flag if the EN16931 digital invoice should be added to the invoice document
     */
    public void exportInvoiceDocument(@NotNull Invoice invoice, @NotNull Path invoiceFolder, boolean withQrCode, boolean withEN16931) {
        InvoiceAsciiDoc asciiDocGenerator = new InvoiceAsciiDoc();
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
    }

    public BigDecimal contractAmountWithoutVat(@NotNull Contract contract, LocalDate from, LocalDate to) {
        return crm.invoices().items().stream().filter(o -> (o.contract().oid() == contract.oid()) && DateUtilities.isWithinRange(o.dueDate(), from, to))
                .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal customerAmountWithoutVat(@NotNull LegalEntity customer, LocalDate from, LocalDate to) {
        return crm.contracts().items().stream().filter(o -> (o.sellee().oid() == customer.oid())).map(o -> contractAmountWithoutVat(o, from, to))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
