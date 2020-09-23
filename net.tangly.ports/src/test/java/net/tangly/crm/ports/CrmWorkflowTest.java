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
import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
class CrmWorkflowTest {
    @Test
    void testCrmTags() {
        TagTypeRegistry registry = new TagTypeRegistry();
        CrmTags.registerTags(registry);
        assertThat(registry.namespaces().size()).isEqualTo(2);
        assertThat(registry.namespaces()).contains(CrmTags.CRM, CrmTags.GEO);
        assertThat(registry.tagNamesForNamespace(CrmTags.GEO)).contains(CrmTags.LATITUDE, CrmTags.LONGITUDE, CrmTags.ALTITUDE, CrmTags.PLUSCODE);
        assertThat(registry.tagNamesForNamespace(CrmTags.CRM).isEmpty()).isFalse();
    }

    @Test
        // @Tag("localTest")
    void testCompanyTsvCrm() {
        CrmWorkflows crmWorkflows = new CrmWorkflows(new Crm());
        Path root = Path.of("/Users/Shared/tangly");
        crmWorkflows.importCrmEntities(root);
        crmWorkflows.crm().invoices().items().forEach(o -> crmWorkflows.exportInvoiceToPdf(root, o));
    }


    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            CrmAndLedgerStore store = new CrmAndLedgerStore(fs);
            store.createCrmAndLedgerRepository();

            CrmWorkflows crmWorkflows = new CrmWorkflows(new Crm());
            crmWorkflows.importCrmEntities(store.crmRoot());

            verifyNaturalEntities(crmWorkflows.crm());
            verifyLegalEntities(crmWorkflows.crm());
            verifyEmployees(crmWorkflows.crm());
            verifyContracts(crmWorkflows.crm());
            verifyProducts(crmWorkflows.crm());
            verifyInteractions(crmWorkflows.crm());
            verifyActivities(crmWorkflows.crm());
            verifySubjects(crmWorkflows.crm());
            verifyComments(crmWorkflows.crm());
            verifyInvoices(crmWorkflows.crm());

            verifyCrmBusinessLogic(crmWorkflows.crm());

            crmWorkflows.exportCrmEntities(store.crmRoot());

            crmWorkflows = new CrmWorkflows(new Crm());
            crmWorkflows.importCrmEntities(store.crmRoot());
            verifyNaturalEntities(crmWorkflows.crm());
            verifyLegalEntities(crmWorkflows.crm());
            verifyEmployees(crmWorkflows.crm());
            verifyContracts(crmWorkflows.crm());
            verifyProducts(crmWorkflows.crm());
            verifyInteractions(crmWorkflows.crm());
            verifyActivities(crmWorkflows.crm());
            verifySubjects(crmWorkflows.crm());
            verifyComments(crmWorkflows.crm());
        }
    }

    private void verifyNaturalEntities(@NotNull Crm crm) {
        assertThat(crm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(crm.naturalEntities().find(1).isPresent()).isTrue();
        assertThat(crm.naturalEntities().findBy(NaturalEntity::id, "jd-01").isPresent()).isTrue();
        crm.naturalEntities().items().forEach(naturalEntity -> assertThat(naturalEntity.isValid()).isTrue());
    }

    private void verifyLegalEntities(@NotNull Crm crm) {
        assertThat(crm.legalEntities().items().isEmpty()).isFalse();
        assertThat(crm.legalEntities().find(100).isPresent()).isTrue();
        assertThat(crm.legalEntities().findBy(LegalEntity::id, "UNKNOWN-100").isPresent()).isTrue();
        assertThat(crm.legalEntities().findBy(LegalEntity::name, "hope llc").isPresent()).isTrue();
        crm.naturalEntities().items().forEach(legalEntity -> assertThat(legalEntity.isValid()).isTrue());
    }

    private void verifyEmployees(@NotNull Crm crm) {
        assertThat(crm.employees().items().isEmpty()).isFalse();
        assertThat(crm.employees().find(200).isPresent()).isTrue();
        crm.employees().items().forEach(employee -> {
            assertThat(employee.oid()).isNotEqualTo(Entity.UNDEFINED_OID);
            assertThat(employee.person()).isNotNull();
            assertThat(employee.organization()).isNotNull();
        });
    }

    private void verifyContracts(@NotNull Crm crm) {
        assertThat(crm.contracts().items().isEmpty()).isFalse();
        assertThat(crm.contracts().find(500).isPresent()).isTrue();
        crm.contracts().items().forEach(contract -> {
            assertThat(contract.locale()).isNotNull();
            assertThat(contract.currency()).isNotNull();
            assertThat(contract.bankConnection()).isNotNull();
            assertThat(contract.seller()).isNotNull();
            assertThat(contract.sellee()).isNotNull();
        });
    }

    private void verifyCrmBusinessLogic(@NotNull Crm crm) {
        CrmBusinessLogic logic = new CrmBusinessLogic(crm);
        crm.contracts().items().forEach(contract -> {
            assertThat(logic.contractPaidAmountWithoutVat(contract, null, null))
                    .isEqualByComparingTo(logic.contractInvoicedAmountWithoutVat(contract, null, null));
            assertThat(logic.contractExpenses(contract, null, null)).isNotNegative();
        });
    }


    private void verifyInvoices(@NotNull Crm crm) {
        assertThat(crm.invoices().items().isEmpty()).isFalse();
        crm.invoices().items().forEach(o -> assertThat(o.isValid()).isTrue());
    }

    private void verifyProducts(@NotNull Crm crm) {
        assertThat(crm.products().items().isEmpty()).isFalse();
    }

    private void verifyInteractions(@NotNull Crm crm) {
        assertThat(crm.interactions().items().isEmpty()).isFalse();
    }

    private void verifyActivities(@NotNull Crm crm) {
        assertThat(crm.activities().items().isEmpty()).isFalse();
    }

    private void verifySubjects(@NotNull Crm crm) {
        assertThat(crm.subjects().items().isEmpty()).isTrue();
    }

    private void verifyComments(@NotNull Crm crm) {
        assertThat(crm.naturalEntities().find(1).orElseThrow().comments().isEmpty()).isFalse();
        assertThat(crm.legalEntities().find(102).orElseThrow().comments().isEmpty()).isFalse();
    }
}
