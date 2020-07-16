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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.core.Entity;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
public class CrmTsvHdlTest {
    private static final String PACKAGE_NAME = "net/tangly/crm/ports/";

    @Test
    void testCsvCrm() throws IOException {
        Crm crm = new Crm();
        CrmTsvHdl handler = new CrmTsvHdl(crm);
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
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


        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.importCrmEntities(directory);
        verifyNaturalEntities(crm);
        verifyLegalEntities(crm);
        verifyEmployees(crm);
        verifyContracts(crm);
    }

    private void verifyNaturalEntities(Crm crm) {
        NaturalEntity naturalEntity = crm.naturalEntities().getAll().get(0);
        assertThat(crm.naturalEntities().getAll().size()).isEqualTo(6);
        assertThat(naturalEntity.oid()).isEqualTo(1);
        assertThat(naturalEntity.id()).isEqualTo("jd-01");
        assertThat(naturalEntity.address(CrmTags.CRM_ADDRESS_HOME)).isNotNull();
        assertThat(naturalEntity.email(CrmTags.HOME)).isNotNull();
        assertThat(naturalEntity.phoneNr(CrmTags.HOME)).isNotNull();
        assertThat(naturalEntity.site(CrmTags.HOME)).isNotNull();
    }

    private void verifyLegalEntities(Crm crm) {
        assertThat(crm.legalEntities().getAll().size()).isEqualTo(3);

        LegalEntity legalEntity = crm.legalEntities().getAll().get(0);
        assertThat(legalEntity.oid()).isEqualTo(100);
        assertThat(legalEntity.id()).isEqualTo("UNKNOWN-100");
        assertThat(legalEntity.name()).isEqualTo("hope llc");
    }

    private void verifyEmployees(Crm crm) {
        assertThat(crm.employees().getAll().size()).isEqualTo(5);
        Employee employee = crm.employees().getAll().get(0);
        assertThat(employee.oid()).isNotEqualTo(Entity.UNDEFINED_OID);
        assertThat(employee.person()).isNotNull();
        assertThat(employee.organization()).isNotNull();
    }

    private void verifyContracts(Crm crm) {
        assertThat(crm.contracts().getAll().size()).isNotEqualTo(0);
        Contract contract = crm.contracts().getAll().get(0);
        assertThat(contract.bankConnection()).isNotNull();
        assertThat(contract.seller()).isNotNull();
        assertThat(contract.sellee()).isNotNull();
    }

    private Path fromResource(String resource) {
        return Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(resource)).getPath());
    }
}
