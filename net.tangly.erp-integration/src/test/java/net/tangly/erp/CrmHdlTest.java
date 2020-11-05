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

package net.tangly.erp;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.core.QualifiedEntity;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.CrmBusinessLogic;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
class CrmHdlTest {
    @Test
    @Tag("localTest")
    void testCompanyTsvCrm() {
        CrmRealm realm = new CrmEntities(new TagTypeRegistry());
        CrmHdl crmHdl = new CrmHdl(realm, Path.of("/Users/Shared/tangly/", "crm"));
        CrmBusinessLogic logic = new CrmBusinessLogic(realm);
        logic.registerTags(crmHdl.realm().tagTypeRegistry());
        crmHdl.importEntities();
    }

    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            CrmHdl crmHdl = new CrmHdl(new CrmEntities(new TagTypeRegistry()), store.crmRoot());
            crmHdl.importEntities();

            verifyNaturalEntities(crmHdl.realm());
            verifyLegalEntities(crmHdl.realm());
            verifyEmployees(crmHdl.realm());
            verifyContracts(crmHdl.realm());
            verifyInteractions(crmHdl.realm());
            verifyActivities(crmHdl.realm());
            verifySubjects(crmHdl.realm());
            verifyComments(crmHdl.realm());

            crmHdl.exportEntities();

            crmHdl = new CrmHdl(new CrmEntities(new TagTypeRegistry()),store.crmRoot());
            crmHdl.importEntities();
            verifyNaturalEntities(crmHdl.realm());
            verifyLegalEntities(crmHdl.realm());
            verifyEmployees(crmHdl.realm());
            verifyContracts(crmHdl.realm());
            verifyInteractions(crmHdl.realm());
            verifyActivities(crmHdl.realm());
            verifySubjects(crmHdl.realm());
            verifyComments(crmHdl.realm());
        }
    }

    private void verifyNaturalEntities(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(crmRealm.naturalEntities().find(1).isPresent()).isTrue();
        crmRealm.naturalEntities().items().forEach(naturalEntity -> assertThat(naturalEntity.isValid()).isTrue());
    }

    private void verifyLegalEntities(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.legalEntities().items().isEmpty()).isFalse();
        assertThat(crmRealm.legalEntities().find(100).isPresent()).isTrue();
        assertThat(crmRealm.legalEntities().findBy(LegalEntity::id, "UNKNOWN-100").isPresent()).isTrue();
        assertThat(crmRealm.legalEntities().findBy(LegalEntity::name, "hope llc").isPresent()).isTrue();
        crmRealm.naturalEntities().items().forEach(legalEntity -> assertThat(legalEntity.isValid()).isTrue());
    }

    private void verifyEmployees(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.employees().items().isEmpty()).isFalse();
        assertThat(crmRealm.employees().find(200).isPresent()).isTrue();
        crmRealm.employees().items().forEach(employee -> {
            assertThat(employee.oid()).isNotEqualTo(QualifiedEntity.UNDEFINED_OID);
            assertThat(employee.person()).isNotNull();
            assertThat(employee.organization()).isNotNull();
        });
    }

    private void verifyContracts(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.contracts().items().isEmpty()).isFalse();
        assertThat(crmRealm.contracts().find(500).isPresent()).isTrue();
        crmRealm.contracts().items().forEach(contract -> {
            assertThat(contract.locale()).isNotNull();
            assertThat(contract.currency()).isNotNull();
            assertThat(contract.bankConnection()).isNotNull();
            assertThat(contract.seller()).isNotNull();
            assertThat(contract.sellee()).isNotNull();
        });
    }

    private void verifyInteractions(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.interactions().items().isEmpty()).isFalse();
    }

    private void verifyActivities(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.collectActivities(o -> true)).isNotEmpty();
    }

    private void verifySubjects(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.subjects().items().isEmpty()).isTrue();
    }

    private void verifyComments(@NotNull CrmRealm crmRealm) {
        assertThat(crmRealm.naturalEntities().find(1).orElseThrow().comments().isEmpty()).isFalse();
        assertThat(crmRealm.legalEntities().find(102).orElseThrow().comments().isEmpty()).isFalse();
    }
}
