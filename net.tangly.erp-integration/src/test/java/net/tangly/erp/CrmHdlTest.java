/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
class CrmHdlTest {
    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            var handler = new CrmHdl(new CrmEntities(), store.crmRoot());
            handler.importEntities();

            verifyNaturalEntities(handler.realm());
            verifyLegalEntities(handler.realm());
            verifyEmployees(handler.realm());
            verifyContracts(handler.realm());
            verifyInteractions(handler.realm());
            verifyActivities(handler.realm());
            verifySubjects(handler.realm());
            verifyActivities(handler.realm());
            verifyComments(handler.realm());

            handler.exportEntities();

            handler = new CrmHdl(new CrmEntities(), store.crmRoot());
            handler.importEntities();
            verifyNaturalEntities(handler.realm());
            verifyLegalEntities(handler.realm());
            verifyEmployees(handler.realm());
            verifyContracts(handler.realm());
            verifyInteractions(handler.realm());
            verifyActivities(handler.realm());
            verifySubjects(handler.realm());
            verifyActivities(handler.realm());
            verifyComments(handler.realm());
        }
    }

    private void verifyNaturalEntities(@NotNull CrmRealm realm) {
        assertThat(realm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.naturalEntities(), 1).isPresent()).isTrue();
        Realm.checkEntities(realm.naturalEntities());
    }

    private void verifyLegalEntities(@NotNull CrmRealm realm) {
        assertThat(realm.legalEntities().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.legalEntities(), 100).isPresent()).isTrue();
        assertThat(realm.legalEntities().findBy(LegalEntity::id, "UNKNOWN-100").isPresent()).isTrue();
        assertThat(realm.legalEntities().findBy(LegalEntity::name, "Vaadin GbmH").isPresent()).isTrue();
        Realm.checkEntities(realm.legalEntities());
    }

    private void verifyEmployees(@NotNull CrmRealm realm) {
        assertThat(realm.employees().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.employees(), 200).isPresent()).isTrue();
        Realm.checkEntities(realm.employees());
    }

    private void verifyContracts(@NotNull CrmRealm realm) {
        assertThat(realm.contracts().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.contracts(), 500).isPresent()).isTrue();
        Realm.checkEntities(realm.contracts());
    }

    private void verifyInteractions(@NotNull CrmRealm realm) {
        assertThat(realm.interactions().items().isEmpty()).isFalse();
        Realm.checkEntities(realm.interactions());
    }

    private void verifyActivities(@NotNull CrmRealm realm) {
        assertThat(realm.collectActivities(o -> true)).isNotEmpty();
        realm.collectActivities(o -> true).forEach(activity -> assertThat(activity.check()).isTrue());
    }

    private void verifySubjects(@NotNull CrmRealm realm) {
        assertThat(realm.subjects().items().isEmpty()).isTrue();
        Realm.checkEntities(realm.subjects());
    }

    private void verifyComments(@NotNull CrmRealm realm) {
        assertThat(Provider.findByOid(realm.naturalEntities(), 1).orElseThrow().comments().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.naturalEntities(), 6).orElseThrow().comments().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.legalEntities(), 102).orElseThrow().comments().isEmpty()).isFalse();
    }
}
