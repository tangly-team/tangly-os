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

package net.tangly.bus.invoices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Currency;
import java.util.List;

import net.tangly.core.Address;
import net.tangly.core.BankConnection;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceTest {
    private static final BigDecimal VAT_REGULAR = new BigDecimal("0.077");
    private static final String PAYMENT_CONDITIONS_30_DAYS = "30 days net";

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
        Article coaching = new Article("0001", "Agile coaching", "", ArticleCode.work, new BigDecimal(1400), "day", VAT_REGULAR);
        Article project = new Article("0002", "Technical project management", "", ArticleCode.work, new BigDecimal("1400"), "day", VAT_REGULAR);

        Invoice invoice = newInvoice("2017-0001", "Coaching contract Planta 20XX-5946 und ARE-20XX-6048");

        invoice.add(new InvoiceItem(1, coaching, "GIS goes Agile project", new BigDecimal("4")));
        invoice.add(new InvoiceItem(2, coaching, "Java architecture coaching project", new BigDecimal("1.5")));
        invoice.add(new Subtotal(3, "Subtotal Project Leading GEO 2017 83200 Planta 20XX-5946", List.of(invoice.getAt(1), invoice.getAt(2))));

        invoice.add(new InvoiceItem(4, project, "OGD technical project management", new BigDecimal("2.25")));
        invoice.add(new Subtotal(5, "Subtotal Agile Coaching 3130 0 80000", List.of(invoice.getAt(4))));

        invoice.paymentConditions(PAYMENT_CONDITIONS_30_DAYS);
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }

    public static Invoice newTeachingInvoice() {
        Article teaching = new Article("0011", "Agile training", "", ArticleCode.work, new BigDecimal(2000), "day", BigDecimal.ZERO);
        Invoice invoice = newInvoice("2017-0002", "Agile Training and Workshop");
        invoice.add(new InvoiceItem(1, teaching, "Scrum Agile Workshop", new BigDecimal("2")));
        invoice.paymentConditions(PAYMENT_CONDITIONS_30_DAYS);
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }

    public static Invoice newComplexInvoice() {
        Article coaching = new Article("0001", "Agile coaching", "", ArticleCode.work, new BigDecimal(1400), "day", VAT_REGULAR);
        Article project = new Article("0002", "Technical project management", "", ArticleCode.work, new BigDecimal("1400"), "day", VAT_REGULAR);
        Article travelExpenses = new Article("9900", "Travel Expenses", "", ArticleCode.expenses, BigDecimal.ONE, "CHF", BigDecimal.ZERO);

        Invoice invoice = newInvoice("2017-0003", "Coaching contract Planta 20XX-5946 und ARE-20XX-6048");

        invoice.add(new InvoiceItem(1, coaching, "GIS goes Agile project", new BigDecimal("4")));
        invoice.add(new InvoiceItem(2, coaching, "Java architecture coaching project", new BigDecimal("1.5")));
        invoice.add(new Subtotal(3, "Subtotal Leading GEO 2017 83200 Planta 20XX-5946", List.of(invoice.getAt(1), invoice.getAt(2))));

        invoice.add(new InvoiceItem(4, project, "OGD technical project management", new BigDecimal("2.25")));
        invoice.add(new Subtotal(5, "Subtotal Agile Coaching 3130 0 80000", List.of(invoice.getAt(4))));

        invoice.add(new InvoiceItem(6, travelExpenses, "Travel Expenses Paris", new BigDecimal("1250")));
        invoice.add(new InvoiceItem(7, travelExpenses, "Travel Expenses Berlin", new BigDecimal("1750")));
        invoice.add(new Subtotal(8, "Travel Expenses (no VAT taxes)", List.of(invoice.getAt(6), invoice.getAt(7))));

        invoice.paymentConditions(PAYMENT_CONDITIONS_30_DAYS);
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }

    private static Invoice newInvoice(String id, String text) {
        Invoice invoice = new Invoice();
        invoice.id(id);
        invoice.name(invoice.id() + "-Invoice");
        invoice.contractId("TEST-CONTRACT-0000");
        invoice.invoicedDate(LocalDate.of(2018, Month.JANUARY, 01));
        invoice.dueDate(LocalDate.of(2018, Month.JANUARY, 31));
        invoice.paidDate((LocalDate.of(2018, Month.FEBRUARY, 10)));
        invoice.invoicingEntity(seller());
        invoice.invoicingAddress(sellerAddress());
        invoice.invoicingConnection(sellerConnection());
        invoice.invoicedEntity(sellee());
        invoice.invoicedAddress(selleeAddress());
        invoice.text(text);
        return invoice;
    }

    private static InvoiceLegalEntity seller() {
        return new InvoiceLegalEntity("CHE-357-875.339", "tangly llc", "CHE-357-875.339 MWST");
    }

    private static Address sellerAddress() {
        return new Address.Builder().street("Lorzenhof 27").postcode("6330").locality("Cham").region("ZG").country("CH").build();
    }

    private static InvoiceLegalEntity sellee() {
        return new InvoiceLegalEntity("CHE-123-456.789", "Flow AG", "CHE-123-456.789 MWST");
    }

    private static Address selleeAddress() {
        return new Address.Builder().extended("attn. John Doe").street("Bahnhofstrasse 1").postcode("6300").locality("Zug").region("ZG").country("CH").build();
    }

    public static BankConnection sellerConnection() {
        return new BankConnection("CH88 0900 0000 3064 1768 2", "POFICHBEXXX", "Postfinanz Schweiz");
    }
}
