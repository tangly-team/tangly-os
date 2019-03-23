/*
 * Copyright 2006-2018 Marcel Baumann
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import net.tangly.commons.models.Address;
import net.tangly.commons.models.HasOid;
import net.tangly.commons.models.PhoneNr;
import net.tangly.erp.crm.models.BankConnection;
import net.tangly.erp.crm.models.CrmTags;
import net.tangly.erp.crm.models.LegalEntity;
import net.tangly.erp.invoices.ports.InvoiceAsciiDoc;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;

/**
 * Created by marcelbaumann on 01.05.17.
 */
public class InvoiceTests {

    @Test
    void writeAsciiDocReport() {
        Invoice invoice = newInvoice();
        InvoiceAsciiDoc report = new InvoiceAsciiDoc(invoice);
        report.create(Paths.get("/Users/Shared/tmp/invoice.adoc"));
        report.generateQCode(Paths.get("/Users/Shared/tmp/invoice.svg"));

        // TODO wait on release ascidoctorj 1.6.0 before continuing testing due to incompabilities with Java 11
        // adoc to pdf, merge invoice with qrcode, generate Zugferd
        // Asciidoctor asciidoctor = create();
        // Map<String, Object> options = options().inPlace(true).backend("pdf").asMap();
        // String html = asciidoctor.convertFile(new File("/Users/Shared/tmp/invoice.adoc"), options);
    }

    @Test
    void writeJsonTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        Invoice invoice = newInvoice();
        String json = writer.writeValueAsString(invoice);
        System.out.println(json);
    }

    private Invoice newInvoice() {
        Product coaching = new Product("0001", "Agile coaching", new BigDecimal(1400));
        Product project = new Product("0002", "Technical project management", new BigDecimal("1400"));

        Address sellerAddress =
                new Address.Builder().street("Bahnhofstrasse 1").postcode("6300").locality("Zug").region("ZG").countryCode("CH").build();
        PhoneNr sellerPhoneNr = new PhoneNr("+41 79 778 8689");

        LegalEntity seller = LegalEntity.of(HasOid.UNDEFINED_OID);
        seller.name("Flow llc");
        seller.address(CrmTags.CRM_ADDRESS_WORK, sellerAddress);
        // seller.setPhoneNr(sellerPhoneNr);
        seller.id("CHE-123-456.789");
        seller.vatNr("CCHE-123-456.789 MWST");

        BankConnection sellerConnection = new BankConnection("CH88 0900 0000 3064 1768 2", "POFICHBEXXX", "Postfinanz Schweiz");

        Invoice invoice = new Invoice("2017-8001");
        invoice.invoicedDate(LocalDate.parse("2018-01-01"));
        invoice.dueDate(LocalDate.parse("2018-01-31"));
        invoice.invoicingEntity(seller);
        invoice.invoicedEntity(seller);
        invoice.invoicingConnection(sellerConnection);

        invoice.text("Beraterverträge Planta 20XX-5946 und ARE-20XX-6048");

        InvoiceItem item = new InvoiceItem(1, coaching, new BigDecimal("4"));
        item.text("GIS goes Agile project");
        invoice.add(item);

        item = new InvoiceItem(2, coaching, new BigDecimal("1.5"));
        item.text("Java architecture coaching project");
        invoice.add(item);

        Subtotal subtotal =
                new Subtotal(3, "Zwischentotal Führungsunterstützung GEO 2017 83200 Planta 20XX-5946", List.of(invoice.getAt(1), invoice.getAt(2)));
        invoice.add(subtotal);

        item = new InvoiceItem(4, coaching, new BigDecimal("2.25"));
        item.text("OGD technical project management");
        invoice.add(item);

        subtotal = new Subtotal(5, "Zwischentotal DL Dritter 3130 0 80000", List.of(invoice.getAt(4)));
        invoice.add(subtotal);

        invoice.paymentConditions("30 Tage netto");
        invoice.vatRate(new BigDecimal("0.077"));
        invoice.currency(Currency.getInstance("CHF"));
        return invoice;
    }
}
