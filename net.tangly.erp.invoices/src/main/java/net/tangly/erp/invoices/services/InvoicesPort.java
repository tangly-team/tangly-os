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

package net.tangly.erp.invoices.services;

import net.tangly.core.Tag;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.Port;
import net.tangly.erp.invoices.domain.Invoice;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.Optional;

/**
 * Defines the import port for the bounded domain invoices. It is the primary port in the DDD terminology.
 */
public interface InvoicesPort extends Port<InvoicesRealm> {
    String INVOICE = "invoice";
    String INVOICE_PATH = "invoicePath";
    String ARTICLES_TSV = "articles.tsv";
    String JSON_EXT = ".json";

    /**
     * Exports an invoice to a file. The method is responsible to infer the uri to the generated invoice document.
     * <p><em>implNote</em> The asciidoc document is deleted upon creation of the pdf document.</p>
     *
     * @param invoice     invoice to be exported
     * @param withQrCode  flag if the Swiss QR cde should be added to the invoice document
     * @param withEN16931 flag if the EN16931 digital invoice should be added to the invoice document
     * @param overwrite   flag if an existing document should be overwritten
     * @see #exportInvoiceDocuments(DomainAudit, boolean, boolean, boolean, LocalDate, LocalDate, String, Collection)
     */
    void exportInvoiceDocument(@NotNull DomainAudit audit, @NotNull Invoice invoice, boolean withQrCode, boolean withEN16931, boolean overwrite,
                               String text, Collection<Tag> tags);

    /**
     * Exports all selected invoices as artifact to a file. The method is responsible to infer the uri to the generated invoice document.
     * <p><em>implNote</em> The asciidoc document is deleted upon creation of the pdf document.</p>
     *
     * @param withQrCode  flag if the Swiss QR cde should be added to the invoice document
     * @param withEN16931 flag if the EN16931 digital invoice should be added to the invoice document
     * @param from        optional start of the relevant time interval for the invoiced date
     * @param to          optional end of the relevant time interval for the invoiced date
     * @param overwrite   flag if an existing document should be overwritten
     */
    void exportInvoiceDocuments(@NotNull DomainAudit audit, boolean withQrCode, boolean withEN16931, boolean overwrite, LocalDate from, LocalDate to,
                                String text, Collection<Tag> tags);

    record InvoiceView(@NotNull String id, @NotNull Currency currency, @NotNull BigDecimal amountWithoutVat, @NotNull BigDecimal vat,
                       @NotNull BigDecimal amountWithVat, @NotNull LocalDate invoicedDate, @NotNull LocalDate dueDate) {
    }

    Optional<InvoiceView> invoiceViewFor(@NotNull String id);
}
