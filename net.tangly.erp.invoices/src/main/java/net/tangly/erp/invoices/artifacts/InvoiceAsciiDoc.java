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

package net.tangly.erp.invoices.artifacts;

import lombok.NonNull;
import net.codecrete.qrbill.generator.Strings;
import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.core.Address;
import net.tangly.core.domain.DocumentGenerator;
import net.tangly.core.domain.DomainAudit;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import net.tangly.erp.invoices.domain.InvoiceLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static net.tangly.commons.utilities.AsciiDocHelper.*;


/**
 * Provides support to generate an AsciiDoc representation of an invoice for the Swiss market. It provides a human-readable invoice document following the VAT invoice constraint,
 * the Swiss invoice QR barcode, and the European Zugferd invoice machine-readable invoice standard.
 */
public class InvoiceAsciiDoc implements DocumentGenerator<Invoice> {
    private static final Logger logger = LogManager.getLogger();
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final ResourceBundle bundle;
    private final Properties properties;

    public InvoiceAsciiDoc(Locale locale, Properties properties) {
        bundle = ResourceBundle.getBundle("net.tangly.erp.invoices.InvoiceAsciiDoc", locale);
        this.properties = properties;
    }

    @Override
    public void export(@NotNull Invoice invoice, boolean overwrite, @NonNull Path document, @NotNull DomainAudit audit) {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(document), true, StandardCharsets.UTF_8)) {
            var helper = new AsciiDocHelper(writer);
            writer.printf(":organization: %s%n", properties.getProperty(ORGANIZATION_NAME_KEY));
            writer.printf(":copyright: Lorzenhof 27, 6330 Cham%n");
            writer.printf(":pdf-themesdir: %s%n", properties.getProperty(THEME_PATH_KEY));
            writer.println(":pdf-theme: tenant");
            writer.println();
            writer.printf("image::%s/tenant-logo.svg[80,80,align=\"center\"]%n", properties.getProperty(LOGO_PATH_KEY));
            writer.println();
            helper.header(bundle.getString("invoice"), 2);

            // Needed to make the image converter of asciiDocPdf happy. Still looking to have relative paths working.

            helper.tableHeader(null, "frame=\"none\", grid=\"none\", options=\"noheader\", stripes=\"none\", cols=\"3,4,3\"");
            helper.tableRow(addressText(invoice.invoicingEntity(), invoice.invoicingEntity().address()), "",
                addressText(invoice.invoicedEntity(), invoice.invoicedEntity().address()));
            helper.tableEnd();

            helper.tableHeader(null, "stripes=\"none\", options=\"noheader\", cols=\"4,2,4,2\"");
            helper.tableRow(bundle.getString("invoiceNumber"), invoice.id(), bundle.getString("invoiceDate"), invoice.date().toString());
            helper.tableRow("", "", bundle.getString("invoiceDueDate"), invoice.dueDate().toString());
            helper.tableEnd();

            writer.printf("*%s*%n", invoice.text());
            writer.println();

            helper.tableHeader(null, "options=\"header\", grid=\"none\", frame=\"none\", stripes=\"none\", cols=\"4,^1, >1,>1\"", bundle.getString("position"),
                bundle.getString("quantity"), bundle.getString("price"), "%s (%s)".formatted(bundle.getString("amount"), invoice.currency().getCurrencyCode()));
            invoice.lines().stream().sorted(Comparator.comparingInt(InvoiceLine::position)).forEach(
                o -> helper.tableRow((o.isAggregate() ? italics(o.text()) : o.text()), o.isItem() ? format(o.quantity(), false) : "",
                    format(o.unitPrice(), false), o.isAggregate() ? italics(format(o.amount(), false)) : format(o.amount(), false)));
            createVatDeclarations(helper, invoice);
            helper.tableEnd();
            writer.println();

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", cols=\"2,4\"");
            helper.tableRow(bundle.getString("bankConnection"),
                "%s: %s +\n%s: %s (%s)".formatted(bundle.getString("iban"), invoice.invoicingConnection().iban(), bundle.getString("bic"),
                    invoice.invoicingConnection().bic(), invoice.invoicingConnection().institute()));
            helper.tableEnd();

            helper.tableHeader(null, "frame=\"none\",grid=\"none\", options=\"noheader\", cols=\"2,4\"");
            helper.tableRow("%s:".formatted(bundle.getString("companyId")), invoice.invoicingEntity().id());
            helper.tableRow("%s:".formatted(bundle.getString("companyVat")), invoice.invoicingEntity().vatNr());
            helper.tableEnd();

            if (!Strings.isNullOrEmpty(invoice.paymentConditions())) {
                writer.println("%s: %s".formatted(bundle.getString("paymentConditions"), invoice.paymentConditions()));
            }
        } catch (Exception e) {
            logger.atError().withThrowable(e).log("Error during invoice asciiDoc generation {}", document);
        }
    }

    private void createVatDeclarations(@NotNull AsciiDocHelper helper, @NotNull Invoice invoice) {
        helper.tableRow("", "", "", "");
        helper.tableRow(bundle.getString("totalWithoutVat"), "", "", format(invoice.amountWithoutVat(), true));
        if (invoice.hasMultipleVatRates()) {
            String vats = invoice.vatAmounts().entrySet().stream().map(
                o -> "%s%% : %s".formatted(o.getKey().multiply(HUNDRED).stripTrailingZeros().setScale(2, RoundingMode.HALF_EVEN).toPlainString(),
                    o.getValue().stripTrailingZeros().toPlainString())).collect(Collectors.joining(", ", "(", ")"));
            helper.tableRow(italics("%s %s".formatted(bundle.getString("vatAmount"), vats)), "", "", italics(format(invoice.vat(), true)));
        } else {
            helper.tableRow(italics(bundle.getString("vatAmount")), "", "%s%%".formatted(
                    italics(invoice.uniqueVatRate().orElseThrow().multiply(HUNDRED).stripTrailingZeros().setScale(2, RoundingMode.HALF_EVEN).toPlainString())),
                italics(format(invoice.vat(), true)));
        }
        helper.tableRow(bold(bundle.getString("total")), "", "", bold(format(invoice.amountWithVat(), true)));
    }

    private static String addressText(@NotNull InvoiceLegalEntity entity, @NotNull Address address) {
        return "%s%s%s%s%s%s %s".formatted(entity.name(), NEWLINE,
            formatIfPresent(Strings.isNullOrEmpty(address.extended()) ? null : AsciiDocHelper.italics(address.extended())), formatIfPresent(address.street()),
            formatIfPresent(address.poBox()), address.postcode(), address.locality());
    }

    private static String formatIfPresent(String value) {
        return Strings.isNullOrEmpty(value) ? "" : "%s%s".formatted(value, NEWLINE);
    }
}
