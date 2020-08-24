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

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import net.tangly.bus.core.Address;
import net.tangly.bus.crm.BankConnection;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceItem;
import net.tangly.bus.invoices.Product;
import net.tangly.bus.invoices.Subtotal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InvoiceTest {
    private static final BigDecimal VAT_REGULAR = new BigDecimal("0.077");
    private static final String PAYMENT_CONDITIONS_30_DAYS = "30 days net";

    @Test
    void writeAsciiDocReport() {
        Path invoicesDir = Paths.get("/tmp/");

        Path invoicePath = invoicesDir.resolve("2017-001-invoice.adoc");
        Path invoiceOutputPath = invoicesDir.resolve("2017-001-invoice.pdf");
        Invoice invoice = newRegularInvoice();
        new InvoiceAsciiDoc().exports(invoice, invoicePath, Collections.emptyMap());
        new InvoiceQrCode().exports(invoice, invoiceOutputPath, Collections.emptyMap());
        new InvoiceZugFerd().exports(invoice, invoiceOutputPath, Collections.emptyMap());

        invoicePath = invoicesDir.resolve("2017-002-invoice.adoc");
        invoiceOutputPath = invoicesDir.resolve("2017-002-invoice.pdf");
        invoice = newTeachingInvoice();
        new InvoiceAsciiDoc().exports(invoice, invoicePath, Collections.emptyMap());
        new InvoiceQrCode().exports(invoice, invoiceOutputPath, Collections.emptyMap());
        new InvoiceZugFerd().exports(invoice, invoiceOutputPath, Collections.emptyMap());

        invoicePath = invoicesDir.resolve("2017-003-invoice.adoc");
        invoiceOutputPath = invoicesDir.resolve("2017-003-invoice.pdf");
        invoice = newComplexInvoice();
        new InvoiceAsciiDoc().exports(invoice, invoicePath, Collections.emptyMap());
        new InvoiceQrCode().exports(invoice, invoiceOutputPath, Collections.emptyMap());
        new InvoiceZugFerd().exports(invoice, invoiceOutputPath, Collections.emptyMap());

        assertThat(Files.exists(invoicesDir)).isTrue();
        assertThat(Files.isDirectory(invoicesDir)).isTrue();
        assertThat(Files.exists(invoicePath)).isTrue();
        assertThat(Files.exists(invoiceOutputPath)).isTrue();
    }

    @Test
    void testRegularInvoiceTotals() {
        Invoice invoice = newRegularInvoice();
        assertThat(invoice.amountWithoutVat()).isEqualByComparingTo(new BigDecimal("10850.00"));
        assertThat(invoice.amountWithVat()).isEqualByComparingTo(new BigDecimal("11685.45"));
        assertThat(invoice.vat()).isEqualByComparingTo(new BigDecimal("835.45"));
    }

    @Test
    void testTeachingTotals() {
        Invoice invoice = newTeachingInvoice();
        assertThat(invoice.amountWithoutVat()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(invoice.amountWithVat()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(invoice.vat()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testComplexInvoiceTotals() {
        Invoice invoice = newComplexInvoice();
        assertThat(invoice.amountWithoutVat()).isEqualByComparingTo(new BigDecimal("13850.00"));
        assertThat(invoice.amountWithVat()).isEqualByComparingTo(new BigDecimal("14685.45"));
        assertThat(invoice.vat()).isEqualByComparingTo(new BigDecimal("835.45"));
    }

    private Invoice newRegularInvoice() {
        Product coaching = new Product("0001", "Agile coaching", "", new BigDecimal(1400), "day", VAT_REGULAR);
        Product project = new Product("0002", "Technical project management", "", new BigDecimal("1400"), "day", VAT_REGULAR);

        Invoice invoice = newInvoice("2017-0001", "Coaching contract Planta 20XX-5946 und ARE-20XX-6048");

        invoice.add(new InvoiceItem(1, coaching, "GIS goes Agile project", new BigDecimal("4")));
        invoice.add(new InvoiceItem(2, coaching, "Java architecture coaching project", new BigDecimal("1.5")));
        invoice.add(new Subtotal(3, "Subtotal Project Leading GEO 2017 83200 Planta 20XX-5946", List.of(invoice.getAt(1), invoice.getAt(2))));

        invoice.add(new InvoiceItem(4, coaching, "OGD technical project management", new BigDecimal("2.25")));
        invoice.add(new Subtotal(5, "Subtotal Agile Coaching 3130 0 80000", List.of(invoice.getAt(4))));

        invoice.paymentConditions(PAYMENT_CONDITIONS_30_DAYS);
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }

    public static Invoice newTeachingInvoice() {
        Product teaching = new Product("0011", "Agile training", "", new BigDecimal(2000), "day", BigDecimal.ZERO);
        Invoice invoice = newInvoice("2017-0002", "Agile Training and Workshop");
        invoice.add(new InvoiceItem(1, teaching, "Scrum Agile Workshop", new BigDecimal("2")));
        invoice.paymentConditions(PAYMENT_CONDITIONS_30_DAYS);
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }

    public static Invoice newComplexInvoice() {
        Product coaching = new Product("0001", "Agile coaching", "", new BigDecimal(1400), "day", VAT_REGULAR);
        Product project = new Product("0002", "Technical project management", "", new BigDecimal("1400"), "day", VAT_REGULAR);
        Product travelExpenses = new Product("9900", "Travel Expenses", "", BigDecimal.ONE, "CHF", BigDecimal.ZERO);

        Invoice invoice = newInvoice("2017-0003", "Coaching contract Planta 20XX-5946 und ARE-20XX-6048");

        invoice.add(new InvoiceItem(1, coaching, "GIS goes Agile project", new BigDecimal("4")));
        invoice.add(new InvoiceItem(2, coaching, "Java architecture coaching project", new BigDecimal("1.5")));
        invoice.add(new Subtotal(3, "Subtotal Project Leading GEO 2017 83200 Planta 20XX-5946", List.of(invoice.getAt(1), invoice.getAt(2))));

        invoice.add(new InvoiceItem(4, coaching, "OGD technical project management", new BigDecimal("2.25")));
        invoice.add(new Subtotal(5, "Subtotal Agile Coaching 3130 0 80000", List.of(invoice.getAt(4))));

        invoice.add(new InvoiceItem(6, travelExpenses, "Travel Expenses Paris", new BigDecimal("1250")));
        invoice.add(new InvoiceItem(7, travelExpenses, "Travel Expenses Berlin", new BigDecimal("1750")));
        invoice.add(new Subtotal(8, "Travel Expenses (no VAT taxes)", List.of(invoice.getAt(6), invoice.getAt(7))));

        invoice.paymentConditions(PAYMENT_CONDITIONS_30_DAYS);
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }

    private static Invoice newInvoice(String id, String text) {
        Contract contract = new Contract();
        contract.id("TEST-CONTRACT-0000");
        contract.sellee(sellee());
        contract.seller(seller());
        contract.sellee().address(CrmTags.WORK).ifPresent(contract::address);
        Invoice invoice = new Invoice();
        invoice.id(id);
        invoice.contract(contract);
        invoice.invoicedDate(LocalDate.parse("2018-01-01"));
        invoice.dueDate(LocalDate.parse("2018-01-31"));
        invoice.invoicingEntity(seller());
        invoice.invoicingAddress(invoice.invoicingEntity().address(CrmTags.WORK).orElse(null));
        invoice.invoicingConnection(sellerConnection());
        invoice.invoicedEntity(sellee());
        invoice.invoicedAddress(invoice.invoicedEntity().address(CrmTags.WORK).orElse(null));
        invoice.text(text);
        return invoice;
    }

    private static LegalEntity seller() {
        LegalEntity seller = new LegalEntity();
        seller.name("tangly llc");
        seller.address(CrmTags.WORK, new Address.Builder().street("Lorzenhof 27").postcode("6330").locality("Cham").region("ZG").country("CH").build());
        seller.phoneNr(CrmTags.WORK, "+41 79 778 8689");
        seller.id("CHE-357-875.339");
        seller.vatNr("CHE-357-875.339 MWST");
        return seller;
    }

    private static LegalEntity sellee() {
        LegalEntity sellee = new LegalEntity();
        sellee.name("Flow AG");
        sellee.address(CrmTags.WORK,
                new Address.Builder().extended("attn. John Doe").street("Bahnhofstrasse 1").postcode("6300").locality("Zug").region("ZG").country("CH")
                        .build());
        sellee.phoneNr(CrmTags.WORK, "+41 41 228 4242");
        sellee.id("CHE-123-456.789");
        sellee.vatNr("CHE-123-456.789 MWST");
        return sellee;
    }

    private static BankConnection sellerConnection() {
        return new BankConnection("CH88 0900 0000 3064 1768 2", "POFICHBEXXX", "Postfinanz Schweiz");
    }
}
