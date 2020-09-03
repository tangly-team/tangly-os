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

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import net.codecrete.qrbill.generator.Strings;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceLine;
import net.tangly.commons.utilities.AsciiDocHelper;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.tangly.commons.utilities.AsciiDocHelper.NEWLINE;
import static net.tangly.commons.utilities.AsciiDocHelper.bold;
import static net.tangly.commons.utilities.AsciiDocHelper.format;
import static net.tangly.commons.utilities.AsciiDocHelper.italics;


/**
 * Provides support to generate a AsciiDoc representation of an invoice for the Swiss market. It provide a human-readable invoice document following the VAT
 * invoice constraint, the Swiss invoice QR barcode, and the European Zugferd invoice machine readable invoice standard.
 */
public class InvoiceAsciiDoc implements InvoiceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceAsciiDoc.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public void exports(@NotNull Invoice invoice, @NotNull Path invoicePath, @NotNull Map<String, Object> properties) {
        try (PrintWriter writer = new PrintWriter(invoicePath.toFile(), StandardCharsets.UTF_8)) {
            AsciiDocHelper helper = new AsciiDocHelper(writer);

            writer.println("image::trefoil.svg[100,100,align=\"center\"]");
            writer.println();
            helper.header("Invoice", 2);

            helper.tableHeader(null, "frame=\"none\", grid=\"none\", options=\"noheader\", stripes=\"none\", cols=\"2,4,2\"");
            helper.tableRow(addressText(invoice.invoicingEntity()), "", addressText(invoice.invoicedEntity()));
            helper.tableEnd();

            helper.tableHeader(null, "stripes=\"none\", options=\"noheader\", cols=\"4,2,4,2\"");
            helper.tableRow("Invoice Number", invoice.id(), "Invoice Date", invoice.invoicedDate().toString());
            helper.tableRow("", "", "Invoice Due Date", invoice.dueDate().toString());
            helper.tableEnd();

            writer.println("*" + invoice.text() + "*");
            writer.println();

            helper.tableHeader(null, "options=\"header\", grid=\"none\", frame=\"none\", stripes=\"none\", cols=\"4,^1, >1,>1\"", "Position", "Quantity",
                    "Price", "Amount (CHF)");
            invoice.lines().stream().sorted(Comparator.comparingInt(InvoiceLine::position)).forEach(o -> helper
                    .tableRow((o.isAggregate() ? italics(o.text()) : o.text()), o.isItem() ? format(o.quantity()) : "", format(o.unitPrice()),
                            o.isAggregate() ? italics(format(o.amount())) : format(o.amount())));
            createVatDeclarations(helper, invoice);
            helper.tableEnd();
            writer.println();

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", cols=\"2,4\"");
            helper.tableRow("Bank Connection",
                    "IBAN: " + invoice.invoicingConnection().iban() + NEWLINE + "BIC: " + invoice.invoicingConnection().bic() + " (" +
                            invoice.invoicingConnection().institute() + ")");
            helper.tableEnd();

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", cols=\"2,4\"");
            helper.tableRow("Company ID:", invoice.invoicedEntity().id());
            helper.tableRow("Company VAT Number:", invoice.invoicedEntity().vatNr());
            helper.tableEnd();

            if (!Strings.isNullOrEmpty(invoice.paymentConditions())) {
                writer.append("Payment Conditions").append(" ").append(invoice.paymentConditions()).println();
            }
        } catch (Exception e) {
            logger.atError().setCause(e).log("Error during invoice asciiDoc generation {}", invoicePath);
        }
        createPdf(invoicePath);
    }

    public static void createPdf(@NotNull Path invoicePath) {
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            Map<String, Object> options = OptionsBuilder.options().inPlace(true).backend("pdf").asMap();
            asciidoctor.convertFile(invoicePath.toFile(), options);
        }
    }

    private static void createVatDeclarations(AsciiDocHelper helper, Invoice invoice) {
        helper.tableRow("", "", "", "");
        helper.tableRow("Total without VAT", "", "", format(invoice.amountWithoutVat()));
        if (invoice.hasMultipleVatRates()) {
            String vats = invoice.vatAmounts().entrySet().stream()
                    .map(o -> o.getKey().multiply(HUNDRED).stripTrailingZeros().toPlainString() + "% : " + o.getValue().stripTrailingZeros().toPlainString())
                    .collect(Collectors.joining(", ", "(", ")"));
            helper.tableRow(italics("VAT Amount " + vats), "", "", italics(format(invoice.vat())));
        } else {
            helper.tableRow(italics("VAT Amount"), "",
                    italics(invoice.uniqueVatRate().orElseThrow().multiply(HUNDRED).stripTrailingZeros().toPlainString()) + "%",
                    italics(format(invoice.vat())));
        }
        helper.tableRow(bold("Total"), "", "", bold(format(invoice.amountWithVat())));
    }

    private static String addressText(@NotNull LegalEntity entity) {
        StringBuilder text = new StringBuilder();
        entity.address(CrmTags.Type.work).ifPresent(
                address -> text.append(entity.name()).append(NEWLINE).append(Strings.isNullOrEmpty(address.extended()) ? "" : (address.extended() + NEWLINE))
                        .append(Strings.isNullOrEmpty(address.street()) ? "" : (address.street() + NEWLINE))
                        .append(Strings.isNullOrEmpty(address.poBox()) ? "" : (address.poBox() + NEWLINE)).append(address.postcode()).append(" ")
                        .append(address.locality()));
        return text.toString();
    }
}
