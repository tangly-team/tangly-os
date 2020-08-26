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

package net.tangly.crm.ports;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.core.Entity;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceItem;
import net.tangly.bus.invoices.Subtotal;
import net.tangly.invoices.ports.InvoiceTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
public class CrmTsvHdlTest {
    private static final String PACKAGE_NAME = "net/tangly/crm/ports/";

    @Test
    void testTsvHdlCrm() throws IOException {
        Crm crm = new Crm();
        CrmTsvHdl handler = new CrmTsvHdl(crm);

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path directory = fs.getPath("/crm/");
            Files.createDirectory(directory);
            Files.createDirectory(directory.resolve("invoices"));

            handler.importNaturalEntities(fromResource(PACKAGE_NAME + CrmWorkflows.NATURAL_ENTITIES_TSV));
            verifyNaturalEntities(crm);
            handler.exportNaturalEntities(directory.resolve(CrmWorkflows.NATURAL_ENTITIES_TSV));
            handler.importNaturalEntities(directory.resolve(CrmWorkflows.NATURAL_ENTITIES_TSV));
            verifyNaturalEntities(crm);

            handler.importLegalEntities(fromResource(PACKAGE_NAME + CrmWorkflows.LEGAL_ENTITIES_TSV));
            verifyLegalEntities(crm);
            handler.exportLegalEntities(directory.resolve(CrmWorkflows.LEGAL_ENTITIES_TSV));
            handler.importLegalEntities(directory.resolve(CrmWorkflows.LEGAL_ENTITIES_TSV));
            verifyLegalEntities(crm);

            handler.importEmployees(fromResource(PACKAGE_NAME + CrmWorkflows.EMPLOYEES_TSV));
            verifyEmployees(crm);
            handler.exportEmployees(directory.resolve(CrmWorkflows.EMPLOYEES_TSV));
            handler.importEmployees(directory.resolve(CrmWorkflows.EMPLOYEES_TSV));
            verifyEmployees(crm);

            handler.importContracts(fromResource(PACKAGE_NAME + CrmWorkflows.CONTRACTS_TSV));
            verifyContracts(crm);
            handler.exportContracts(directory.resolve(CrmWorkflows.CONTRACTS_TSV));
            handler.importContracts(directory.resolve(CrmWorkflows.CONTRACTS_TSV));
            verifyContracts(crm);

            handler.importProducts(fromResource(PACKAGE_NAME + CrmWorkflows.PRODUCTS_TSV));
            handler.exportProducts(directory.resolve(CrmWorkflows.PRODUCTS_TSV));
            handler.importProducts(directory.resolve(CrmWorkflows.PRODUCTS_TSV));

            handler.importInteractions(fromResource(PACKAGE_NAME + CrmWorkflows.INTERACTIONS_TSV));
            handler.exportInteractions(directory.resolve(CrmWorkflows.INTERACTIONS_TSV));
            handler.importInteractions(directory.resolve(CrmWorkflows.INTERACTIONS_TSV));

            handler.importActivities(fromResource(PACKAGE_NAME + CrmWorkflows.ACTIVITIES_TSV));
            handler.exportActivities(directory.resolve(CrmWorkflows.ACTIVITIES_TSV));
            handler.importActivities(directory.resolve(CrmWorkflows.ACTIVITIES_TSV));

            handler.importSubjects(fromResource(PACKAGE_NAME + CrmWorkflows.SUBJECTS_TSV));
            handler.exportSubjects(directory.resolve(CrmWorkflows.SUBJECTS_TSV));
            handler.importSubjects(directory.resolve(CrmWorkflows.SUBJECTS_TSV));

            handler.importComments(fromResource(PACKAGE_NAME + CrmWorkflows.COMMENTS_TSV));
            handler.exportComments(directory.resolve(CrmWorkflows.COMMENTS_TSV));
            handler.importComments(directory.resolve(CrmWorkflows.COMMENTS_TSV));

            crm.invoices().update(create(crm));

            CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
            crmWorkflows.exportCrmEntities(directory);

            crmWorkflows = new CrmWorkflows(new Crm());

            crmWorkflows.importCrmEntities(directory);
            verifyNaturalEntities(crmWorkflows.crm());
            verifyLegalEntities(crmWorkflows.crm());
            verifyEmployees(crmWorkflows.crm());
            verifyContracts(crmWorkflows.crm());
            verifyInvoices(crmWorkflows.crm());
        }
    }

    private void verifyNaturalEntities(@NotNull Crm crm) {
        assertThat(crm.naturalEntities().getAll().size()).isEqualTo(6);
        assertThat(crm.naturalEntities().getAll().get(0).oid()).isEqualTo(1);
        assertThat(crm.naturalEntities().getAll().get(0).id()).isEqualTo("jd-01");
        crm.naturalEntities().getAll().forEach(naturalEntity -> assertThat(naturalEntity.isValid()).isTrue());
    }

    private void verifyLegalEntities(@NotNull Crm crm) {
        assertThat(crm.legalEntities().getAll().size()).isEqualTo(3);
        assertThat(crm.legalEntities().getAll().get(0).oid()).isEqualTo(100);
        assertThat(crm.legalEntities().getAll().get(0).id()).isEqualTo("UNKNOWN-100");
        assertThat(crm.legalEntities().getAll().get(0).name()).isEqualTo("hope llc");
        crm.naturalEntities().getAll().forEach(legalEntity -> assertThat(legalEntity.isValid()).isTrue());
    }

    private void verifyEmployees(@NotNull Crm crm) {
        assertThat(crm.employees().getAll().size()).isEqualTo(5);
        crm.employees().getAll().forEach(employee -> {
            assertThat(employee.oid()).isNotEqualTo(Entity.UNDEFINED_OID);
            assertThat(employee.person()).isNotNull();
            assertThat(employee.organization()).isNotNull();
        });
    }

    private void verifyContracts(@NotNull Crm crm) {
        assertThat(crm.contracts().getAll().size()).isNotEqualTo(0);
        crm.contracts().getAll().forEach(contract -> {
            assertThat(contract.bankConnection()).isNotNull();
            assertThat(contract.seller()).isNotNull();
            assertThat(contract.sellee()).isNotNull();
        });
    }

    private void verifyInvoices(@NotNull Crm crm) {
        assertThat(crm.invoices().getAll().isEmpty()).isFalse();
    }

    private Invoice create(@NotNull Crm crm) {
        Invoice invoice = new Invoice();
        invoice.id("2020-0001");
        invoice.name("2020-0001-Invoice-test");
        invoice.text("text of invoice 2020-001");
        invoice.contract(crm.contracts().find(500).orElseThrow());
        invoice.currency(Currency.getInstance("CHF"));
        invoice.deliveryDate(LocalDate.parse("2020-01-01"));
        invoice.invoicedDate(LocalDate.parse("2020-01-01"));
        invoice.dueDate(LocalDate.parse("2020-02-01"));
        invoice.invoicingEntity(crm.legalEntities().find(102).orElseThrow());
        invoice.invoicingAddress(invoice.invoicingEntity().address(CrmTags.WORK).orElse(null));
        invoice.invoicingConnection(InvoiceTest.sellerConnection());
        invoice.invoicedEntity(crm.legalEntities().find(101).orElseThrow());
        invoice.invoicedAddress(invoice.invoicedEntity().address(CrmTags.WORK).orElse(null));

        invoice.add(new InvoiceItem(1, crm.products().find("1").orElseThrow(), "position 1", new BigDecimal("10")));
        invoice.add(new InvoiceItem(2, crm.products().find("2").orElseThrow(), "position 2", new BigDecimal("20.5")));
        invoice.add(new Subtotal(3, "Subtotal position 3", List.of(invoice.getAt(1), invoice.getAt(2))));
        return invoice;
    }

    private Path fromResource(String resource) {
        return Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(resource)).getPath());
    }
}
