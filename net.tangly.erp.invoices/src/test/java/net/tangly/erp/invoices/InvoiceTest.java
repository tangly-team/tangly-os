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

package net.tangly.erp.invoices;

import net.tangly.core.Address;
import net.tangly.core.BankConnection;
import net.tangly.core.EmailAddress;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.erp.invoices.artifacts.InvoiceJson;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.ArticleCode;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceItem;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import net.tangly.erp.invoices.domain.Subtotal;
import net.tangly.erp.invoices.services.InvoicesRealm;
import net.tangly.gleam.model.JsonEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceTest {
    private static final BigDecimal VAT_REGULAR = new BigDecimal("0.077");
    private static final String PAYMENT_CONDITIONS_30_DAYS = "30 days net";

    record Realm(Provider<Invoice> invoices, Provider<Article> articles, Provider<InvoiceLegalEntity> legalEntities) implements InvoicesRealm {
        @Override
        public void close() throws Exception {
        }
    }

    @Test
    void testRegularInvoiceTotals() {
        var realm = new Realm(ProviderInMemory.of(), ProviderInMemory.of(), ProviderInMemory.of());
        Invoice invoice = newRegularInvoice(realm);
        assertThat(invoice.amountWithoutVat()).isEqualByComparingTo(new BigDecimal("10850.00"));
        assertThat(invoice.amountWithVat()).isEqualByComparingTo(new BigDecimal("11685.45"));
        assertThat(invoice.vat()).isEqualByComparingTo(new BigDecimal("835.45"));
    }

    @Test
    void testTeachingTotals() {
        var invoice = newTeachingInvoice();
        assertThat(invoice.amountWithoutVat()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(invoice.amountWithVat()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(invoice.vat()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testComplexInvoiceTotals() {
        var invoice = newComplexInvoice();
        assertThat(invoice.amountWithoutVat()).isEqualByComparingTo(new BigDecimal("13850.00"));
        assertThat(invoice.amountWithVat()).isEqualByComparingTo(new BigDecimal("14685.45"));
        assertThat(invoice.vat()).isEqualByComparingTo(new BigDecimal("835.45"));
    }

    @Test
    void testJsonRegualarInvoice() {
        var realm = new Realm(ProviderInMemory.of(), ProviderInMemory.of(), ProviderInMemory.of());
        var invoice = newRegularInvoice(realm);
        var invoiceJson = new InvoiceJson(realm);
        JsonEntity<Invoice> entity = invoiceJson.createJsonInvoice();
        var jsonObject = entity.exports(invoice);
        var importedInvoice = entity.imports(jsonObject);

        assertThat(importedInvoice.name()).isEqualTo(invoice.name());
        assertThat(importedInvoice.contractId()).isEqualTo(invoice.contractId());
        assertThat(importedInvoice.date()).isEqualTo(invoice.date());
        assertThat(importedInvoice.deliveryDate()).isEqualTo(invoice.deliveryDate());
        assertThat(importedInvoice.dueDate()).isEqualTo(invoice.dueDate());
        assertThat(importedInvoice.paidDate()).isEqualTo(invoice.paidDate());
        assertThat(importedInvoice.invoicingEntity()).isEqualTo(invoice.invoicingEntity());
        assertThat(importedInvoice.invoicedEntity()).isEqualTo(invoice.invoicedEntity());
        assertThat(importedInvoice.invoicingConnection()).isEqualTo(invoice.invoicingConnection());
        assertThat(importedInvoice.currency()).isEqualTo(invoice.currency());
        assertThat(importedInvoice.locale()).isEqualTo(invoice.locale());
        assertThat(importedInvoice.paymentConditions()).isEqualTo(invoice.paymentConditions());
    }

    private Invoice newRegularInvoice(InvoicesRealm realm) {
        realm.articles().update(new Article("0001", "Agile coaching", "", ArticleCode.work, new BigDecimal(1400), "day", VAT_REGULAR));
        realm.articles().update(new Article("0002", "Technical project management", "", ArticleCode.work, new BigDecimal("1400"), "day", VAT_REGULAR));

        Invoice invoice = newInvoice("2017-0001", "Coaching contract Planta 20XX-5946 und ARE-20XX-6048");
        invoice.add(new InvoiceItem(1, realm.articles().findBy(Article::id, "0001").orElseThrow(), "GIS goes Agile project", new BigDecimal("4")));
        invoice.add(
            new InvoiceItem(2, realm.articles().findBy(Article::id, "0001").orElseThrow(), "Java architecture coaching project", new BigDecimal("1.5")));
        invoice.add(new Subtotal(3, "Subtotal Project Leading GEO 2017 83200 Planta 20XX-5946", List.of(invoice.getAt(1), invoice.getAt(2))));
        invoice.add(new InvoiceItem(4, realm.articles().findBy(Article::id, "0002").orElseThrow(), "OGD technical project management", new BigDecimal("2.25")));
        invoice.add(new Subtotal(5, "Subtotal Agile Coaching 3130 0 80000", List.of(invoice.getAt(4))));
        invoice.paymentConditions(PAYMENT_CONDITIONS_30_DAYS);
        invoice.currency(Currency.getInstance("CHF"));
        realm.invoices().update(invoice);
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
        invoice.date(LocalDate.of(2018, Month.JANUARY, 01));
        invoice.deliveryDate(LocalDate.of(2018, Month.JANUARY, 1));
        invoice.dueDate(LocalDate.of(2018, Month.JANUARY, 31));
        invoice.paidDate((LocalDate.of(2018, Month.FEBRUARY, 10)));
        invoice.invoicingEntity(seller());
        invoice.invoicingConnection(sellerConnection());
        invoice.invoicedEntity(sellee());
        invoice.text(text);
        return invoice;
    }

    private static InvoiceLegalEntity seller() {
        var sellerAddress = Address.builder().street("Lorzenhof 27").postcode("6330").locality("Cham").region("ZG").country("CH").build();
        return new InvoiceLegalEntity("CHE-357-875.339", "tangly llc", "CHE-357-875.339 MWST", sellerAddress, EmailAddress.of("info@tangly.net"));
    }

    private static InvoiceLegalEntity sellee() {
        var selleAddress =
            Address.builder().extended("attn. John Doe").street("Bahnhofstrasse 1").postcode("6300").locality("Zug").region("ZG").country("CH").build();
        return new InvoiceLegalEntity("CHE-123-456.789", "Flow AG", "CHE-123-456.789 MWST", selleAddress, EmailAddress.of("finances@flow.com"));
    }

    private static BankConnection sellerConnection() {
        return new BankConnection("CH88 0900 0000 3064 1768 2", "POFICHBEXXX", "Postfinanz Schweiz");
    }
}
