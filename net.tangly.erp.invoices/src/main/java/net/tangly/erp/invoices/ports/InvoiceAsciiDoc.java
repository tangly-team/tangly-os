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

package net.tangly.erp.invoices.ports;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import net.codecrete.qrbill.generator.Strings;
import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.core.Address;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import net.tangly.erp.invoices.domain.InvoiceLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static net.tangly.commons.utilities.AsciiDocHelper.NEWLINE;
import static net.tangly.commons.utilities.AsciiDocHelper.bold;
import static net.tangly.commons.utilities.AsciiDocHelper.format;
import static net.tangly.commons.utilities.AsciiDocHelper.italics;


/**
 * Provides support to generate a AsciiDoc representation of an invoice for the Swiss market. It provide a human-readable invoice document following the VAT
 * invoice constraint, the Swiss invoice QR barcode, and the European Zugferd invoice machine readable invoice standard.
 */
public class InvoiceAsciiDoc implements InvoiceGenerator {
    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final ResourceBundle bundle;

    public InvoiceAsciiDoc(Locale locale) {
        bundle = ResourceBundle.getBundle("net.tangly.invoices.ports.InvoiceAsciiDoc", locale);
    }

    @Override
    public void exports(@NotNull Invoice invoice, @NotNull Path invoicePath, @NotNull Map<String, Object> properties) {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(invoicePath), true, StandardCharsets.UTF_8)) {
            var helper = new AsciiDocHelper(writer);
            writer.println(":imagesdir: ../..");
            writer.println();
            writer.println("image::trefoil.svg[100,100,align=\"center\"]");
            writer.println();
            helper.header(bundle.getString("invoice"), 2);

            helper.tableHeader(null, "frame=\"none\", grid=\"none\", options=\"noheader\", stripes=\"none\", cols=\"3,4,3\"");
            helper.tableRow(addressText(invoice.invoicingEntity(), invoice.invoicingEntity().address()), "",
                addressText(invoice.invoicedEntity(), invoice.invoicedEntity().address()));
            helper.tableEnd();

            helper.tableHeader(null, "stripes=\"none\", options=\"noheader\", cols=\"4,2,4,2\"");
            helper.tableRow(bundle.getString("invoiceNumber"), invoice.id(), bundle.getString("invoiceDate"), invoice.date().toString());
            helper.tableRow("", "", bundle.getString("invoiceDueDate"), invoice.dueDate().toString());
            helper.tableEnd();

            writer.println("*" + invoice.text() + "*");
            writer.println();

            helper.tableHeader(null, "options=\"header\", grid=\"none\", frame=\"none\", stripes=\"none\", cols=\"4,^1, >1,>1\"", bundle.getString("position"),
                bundle.getString("quantity"), bundle.getString("price"), bundle.getString("amount") + " (" + invoice.currency().getCurrencyCode() + ")");
            invoice.lines().stream().sorted(Comparator.comparingInt(InvoiceLine::position)).forEach(
                o -> helper.tableRow((o.isAggregate() ? italics(o.text()) : o.text()), o.isItem() ? format(o.quantity()) : "", format(o.unitPrice()),
                    o.isAggregate() ? italics(format(o.amount())) : format(o.amount())));
            createVatDeclarations(helper, invoice);
            helper.tableEnd();
            writer.println();

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", cols=\"2,4\"");
            helper.tableRow(bundle.getString("bankConnection"),
                bundle.getString("iban") + ": " + invoice.invoicingConnection().iban() + NEWLINE + bundle.getString("bic") + ": " +
                    invoice.invoicingConnection().bic() + " (" + invoice.invoicingConnection().institute() + ")");
            helper.tableEnd();

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", cols=\"2,4\"");
            helper.tableRow(bundle.getString("companyId") + ":", invoice.invoicedEntity().id());
            helper.tableRow(bundle.getString("companyVat") + ":", invoice.invoicingEntity().vatNr());
            helper.tableEnd();

            if (!Strings.isNullOrEmpty(invoice.paymentConditions())) {
                writer.append(bundle.getString("paymentConditions")).append(" ").append(invoice.paymentConditions()).println();
            }
        } catch (Exception e) {
            logger.atError().withThrowable(e).log("Error during invoice asciiDoc generation {}", invoicePath);
        }
    }

    private void createVatDeclarations(@NotNull AsciiDocHelper helper, @NotNull Invoice invoice) {
        helper.tableRow("", "", "", "");
        helper.tableRow(bundle.getString("totalWithoutVat"), "", "", format(invoice.amountWithoutVat()));
        if (invoice.hasMultipleVatRates()) {
            String vats = invoice.vatAmounts().entrySet().stream()
                .map(o -> o.getKey().multiply(HUNDRED).stripTrailingZeros().toPlainString() + "% : " + o.getValue().stripTrailingZeros().toPlainString())
                .collect(Collectors.joining(", ", "(", ")"));
            helper.tableRow(italics(bundle.getString("vatAmount") + " " + vats), "", "", italics(format(invoice.vat())));
        } else {
            helper.tableRow(italics(bundle.getString("vatAmount")), "",
                italics(invoice.uniqueVatRate().orElseThrow().multiply(HUNDRED).stripTrailingZeros().toPlainString()) + "%", italics(format(invoice.vat())));
        }
        helper.tableRow(bold(bundle.getString("total")), "", "", bold(format(invoice.amountWithVat())));
    }

    private static String addressText(@NotNull InvoiceLegalEntity entity, @NotNull Address address) {
        return entity.name() + NEWLINE + (Strings.isNullOrEmpty(address.extended()) ? "" : (address.extended() + NEWLINE)) +
            (Strings.isNullOrEmpty(address.street()) ? "" : (address.street() + NEWLINE)) +
            (Strings.isNullOrEmpty(address.poBox()) ? "" : (address.poBox() + NEWLINE)) + address.postcode() + " " + address.locality();
    }
}
