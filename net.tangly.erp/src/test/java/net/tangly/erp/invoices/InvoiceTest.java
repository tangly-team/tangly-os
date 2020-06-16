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

package net.tangly.erp.invoices;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import net.tangly.bus.core.Address;
import net.tangly.bus.core.HasOid;
import net.tangly.bus.crm.BankConnection;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceItem;
import net.tangly.bus.invoices.Product;
import net.tangly.bus.invoices.Subtotal;
import net.tangly.erp.invoices.ports.InvoiceAsciiDoc;
import net.tangly.erp.invoices.ports.InvoiceQrCode;
import net.tangly.erp.invoices.ports.InvoiceZugFerd;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class InvoiceTest {
    @Test
    void writeAsciiDocReport() throws IOException {
        Path invoicesDir = Paths.get("/tmp/");
        Path invoicePath = invoicesDir.resolve("invoice.adoc");
        Path invoiceOutputPath = invoicesDir.resolve("invoice.pdf");

        Invoice invoice = newInvoice();
        new InvoiceAsciiDoc().create(invoice, invoicePath, Collections.emptyMap());
        new InvoiceQrCode().create(invoice, invoiceOutputPath, Collections.emptyMap());
        new InvoiceZugFerd().create(invoice, invoiceOutputPath, Collections.emptyMap());

        assertThat(Files.exists(invoicesDir)).isTrue();
        assertThat(Files.isDirectory(invoicesDir)).isTrue();
        assertThat(Files.exists(invoicePath)).isTrue();
        assertThat(Files.exists(invoiceOutputPath)).isTrue();
    }

    @Test
    void writeJsonTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        Invoice invoice = newInvoice();
        String json = writer.writeValueAsString(invoice);
    }

    private Invoice newInvoice() {
        Product coaching = new Product("0001", "Agile coaching", new BigDecimal(1400), "day");
        Product project = new Product("0002", "Technical project management", new BigDecimal("1400"), "day");

        LegalEntity seller = LegalEntity.of(HasOid.UNDEFINED_OID);
        seller.name("tangly llc");
        seller.address(CrmTags.CRM_ADDRESS_WORK,
                new Address.Builder().street("Lorzenhof 27").postcode("6330").locality("Cham").region("ZG").country("CH").build()
        );
        seller.setPhoneNr(CrmTags.CRM_PHONE_WORK, "+41 79 778 8689");
        seller.id("CHE-357-875.339");
        seller.vatNr("CHE-357-875.339 MWST");

        BankConnection sellerConnection = new BankConnection("CH88 0900 0000 3064 1768 2", "POFICHBEXXX", "Postfinanz Schweiz");

        LegalEntity sellee = LegalEntity.of(HasOid.UNDEFINED_OID);
        sellee.name("Flow llc");
        sellee.address(CrmTags.CRM_ADDRESS_WORK,
                new Address.Builder().extended("attn. John Doe").street("Bahnhofstrasse 1").postcode("6300").locality("Zug").region("ZG")
                        .country("CH").build()
        );
        sellee.setPhoneNr(CrmTags.CRM_PHONE_WORK, "+41 41 228 4242");
        sellee.id("CHE-123-456.789");
        sellee.vatNr("CHE-123-456.789 MWST");

        Invoice invoice = new Invoice();
        invoice.id("2017-0001");
        invoice.invoicedDate(LocalDate.parse("2018-01-01"));
        invoice.dueDate(LocalDate.parse("2018-01-31"));
        invoice.invoicingEntity(seller);
        invoice.invoicedEntity(sellee);
        invoice.invoicingConnection(sellerConnection);
        invoice.text("Coaching contract Planta 20XX-5946 und ARE-20XX-6048");

        invoice.add(new InvoiceItem(1, coaching, "GIS goes Agile project", new BigDecimal("4")));
        invoice.add(new InvoiceItem(2, coaching, "Java architecture coaching project", new BigDecimal("1.5")));
        invoice.add(new Subtotal(3, "Subtotal Project Leading GEO 2017 83200 Planta 20XX-5946", List.of(invoice.getAt(1), invoice.getAt(2))));
        invoice.add(new InvoiceItem(4, coaching, "OGD technical project management", new BigDecimal("2.25")));
        invoice.add(new Subtotal(5, "Subtotal Agile Coaching 3130 0 80000", List.of(invoice.getAt(4))));

        invoice.paymentConditions("30 day netto");
        invoice.vatRate(new BigDecimal("0.077"));
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }
}
