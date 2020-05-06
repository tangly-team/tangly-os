/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.erp.invoices.ports;

import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.BillFormat;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;
import net.codecrete.qrbill.generator.QRBill;
import net.codecrete.qrbill.generator.Strings;
import net.tangly.bus.core.Address;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceLine;
import net.tangly.commons.utilities.AsciiDocHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static net.tangly.commons.utilities.AsciiDocHelper.NEWLINE;
import static net.tangly.commons.utilities.AsciiDocHelper.format;

/**
 * Provides support to generate a AsciiDoc representation of an invoice for the Swiss market. It provide a human-readable invoice document following
 * the VAT invoice constraint, the Swiss invoice QR barcode, and the European Zugferd invoice machine readable invoice standard.
 */
public class InvoiceAsciiDoc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InvoiceAsciiDoc.class);
    private Invoice invoice;

    public InvoiceAsciiDoc(Invoice invoice) {
        this.invoice = invoice;
    }

    public void create(@NotNull Path path) {
        try (PrintWriter writer = new PrintWriter(path.toFile(), StandardCharsets.UTF_8)) {
            // TODO i18n, l16n
            AsciiDocHelper helper = new AsciiDocHelper(writer);

            helper.header("Invoice", 1);

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", stripes=\"none\", cols=\"2,4,2\"");
            helper.tableRow(text(invoice.invoicingEntity()), "", text(invoice.invoicedEntity()));
            helper.tableEnd();

            helper.tableHeader(null, "stripes=\"none\", options=\"noheader\", cols=\"4,2,4,2\"");
            helper.tableRow("Rechnungsnummer", invoice.id(), "Rechnungsdatum", invoice.invoicedDate().toString());
            helper.tableRow("", "", "Rechnungsfälligkeit", invoice.dueDate().toString());
            helper.tableEnd();

            writer.println(invoice.text());

            helper.tableHeader(null, "options=\"header\", stripes=\"none\", cols=\"4,^1, >1,>1\"", "Bezeichnung", "Menge", "Preis", "Betrag (CHF)");
            invoice.items().stream().sorted(Comparator.comparingInt(InvoiceLine::position))
                    .forEach(o -> helper.tableRow(o.text(), o.isRawItem() ? format(o.quantity()) : "", format(o.unitPrice()), format(o.amount())));
            helper.tableRow("Total without VAT", "", "", format(invoice.amountWithoutVat()));
            helper.tableRow("VAT Amount", "", format(invoice.vatRate().multiply(new BigDecimal("100"))) + "%", format(invoice.amountVat()));
            helper.tableRow("Total", "", "", format(invoice.amountWithVat()));
            helper.tableEnd();

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", cols=\"2,4\"");
            helper.tableRow("Bankverbindung",
                    "IBAN: " + invoice.invoicingConnection().iban() + NEWLINE + "BIC: " + invoice.invoicingConnection().bic() + "(" +
                            invoice.invoicingConnection().institute() + ")");
            helper.tableEnd();

            writer.append("Company ID and VAT Nummer").append(" ").append(invoice.invoicedEntity().id()).println();
            writer.println();

            if (!Strings.isNullOrEmpty(invoice.paymentConditions())) {
                writer.append("Zahlungsbedingung").append(" ").append(invoice.paymentConditions()).println();
            }
        } catch (Exception e) {
            log.error("Error during reporting", e);
        }
    }

    public void generateQCode(@NotNull Path path) {
        Bill bill = new Bill();
        BillFormat format = new BillFormat();
        format.setLanguage(Language.EN);
        format.setOutputSize(OutputSize.A4_PORTRAIT_SHEET);
        format.setGraphicsFormat(GraphicsFormat.SVG);
        bill.setFormat(format);
        bill.setVersion(Bill.Version.V2_0);

        bill.setCreditor(create(invoice.invoicingEntity()));
        bill.setDebtor(create(invoice.invoicedEntity()));

        bill.setAccount(invoice.invoicingConnection().iban());
        bill.setAmount(invoice.amountWithVat());
        bill.setCurrency(invoice.currency().getCurrencyCode());


        bill.setReference(null);
        bill.setUnstructuredMessage(invoice.id());

        byte[] svg = QRBill.generate(bill);

        try {
            Files.write(path, svg);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String text(@NotNull LegalEntity entity) {
        StringBuilder text = new StringBuilder();
        Address address = entity.address(CrmTags.CRM_ADDRESS_WORK);
        text.append(entity.name()).append(NEWLINE).append(Strings.isNullOrEmpty(address.street()) ? "" : address.street() + NEWLINE)
                .append(Strings.isNullOrEmpty(address.poBox()) ? "" : address.poBox() + NEWLINE).append(address.locality());
        return text.toString();
    }

    private static net.codecrete.qrbill.generator.Address create(@NotNull LegalEntity entity) {
        Address address = entity.address(CrmTags.CRM_ADDRESS_WORK);
        net.codecrete.qrbill.generator.Address qrAddress = new net.codecrete.qrbill.generator.Address();
        qrAddress.setName(entity.name());
        qrAddress.setStreet(address.street());
        qrAddress.setHouseNo(null);
        qrAddress.setPostalCode(address.postcode());
        qrAddress.setTown(address.locality());
        qrAddress.setCountryCode(address.country());
        return qrAddress;
    }
}
