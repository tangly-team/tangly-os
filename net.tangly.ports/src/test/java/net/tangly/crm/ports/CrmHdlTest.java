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
import net.tangly.bus.core.QualifiedEntity;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.BusinessLogicCrm;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.RealmCrm;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
class CrmHdlTest {
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
        CrmHdl crmHdl = new CrmHdl(new CrmEntities(new TagTypeRegistry()));
        BusinessLogicCrm logic = new BusinessLogicCrm(crmHdl.realm());
        logic.registerTags(crmHdl.realm().tagTypeRegistry());
        crmHdl.importEntities(Path.of("/Users/Shared/tangly/", "crm"));
    }

    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            CrmAndLedgerStore store = new CrmAndLedgerStore(fs);
            store.createCrmAndLedgerRepository();

            CrmHdl crmHdl = new CrmHdl(new CrmEntities(new TagTypeRegistry()));
            crmHdl.importEntities(store.crmRoot());

            verifyNaturalEntities(crmHdl.realm());
            verifyLegalEntities(crmHdl.realm());
            verifyEmployees(crmHdl.realm());
            verifyContracts(crmHdl.realm());
            verifyInteractions(crmHdl.realm());
            verifyActivities(crmHdl.realm());
            verifySubjects(crmHdl.realm());
            verifyComments(crmHdl.realm());

            crmHdl.exportEntities(store.crmRoot());

            crmHdl = new CrmHdl(new CrmEntities(new TagTypeRegistry()));
            crmHdl.importEntities(store.crmRoot());
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

    private void verifyNaturalEntities(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(realmCrm.naturalEntities().find(1).isPresent()).isTrue();
        realmCrm.naturalEntities().items().forEach(naturalEntity -> assertThat(naturalEntity.isValid()).isTrue());
    }

    private void verifyLegalEntities(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.legalEntities().items().isEmpty()).isFalse();
        assertThat(realmCrm.legalEntities().find(100).isPresent()).isTrue();
        assertThat(realmCrm.legalEntities().findBy(LegalEntity::id, "UNKNOWN-100").isPresent()).isTrue();
        assertThat(realmCrm.legalEntities().findBy(LegalEntity::name, "hope llc").isPresent()).isTrue();
        realmCrm.naturalEntities().items().forEach(legalEntity -> assertThat(legalEntity.isValid()).isTrue());
    }

    private void verifyEmployees(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.employees().items().isEmpty()).isFalse();
        assertThat(realmCrm.employees().find(200).isPresent()).isTrue();
        realmCrm.employees().items().forEach(employee -> {
            assertThat(employee.oid()).isNotEqualTo(QualifiedEntity.UNDEFINED_OID);
            assertThat(employee.person()).isNotNull();
            assertThat(employee.organization()).isNotNull();
        });
    }

    private void verifyContracts(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.contracts().items().isEmpty()).isFalse();
        assertThat(realmCrm.contracts().find(500).isPresent()).isTrue();
        realmCrm.contracts().items().forEach(contract -> {
            assertThat(contract.locale()).isNotNull();
            assertThat(contract.currency()).isNotNull();
            assertThat(contract.bankConnection()).isNotNull();
            assertThat(contract.seller()).isNotNull();
            assertThat(contract.sellee()).isNotNull();
        });
    }

    private void verifyInteractions(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.interactions().items().isEmpty()).isFalse();
    }

    private void verifyActivities(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.collectActivities(o -> true)).isNotEmpty();
    }

    private void verifySubjects(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.subjects().items().isEmpty()).isTrue();
    }

    private void verifyComments(@NotNull RealmCrm realmCrm) {
        assertThat(realmCrm.naturalEntities().find(1).orElseThrow().comments().isEmpty()).isFalse();
        assertThat(realmCrm.legalEntities().find(102).orElseThrow().comments().isEmpty()).isFalse();
    }
}
