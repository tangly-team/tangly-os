/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmEntities;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class CrmPersistenceTest {
    @Test
    void persistCrmRealmToDbTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var crmDb = store.dbRoot().resolve(CrmBoundedDomain.DOMAIN);
            var crmData = store.dataRoot().resolve(CrmBoundedDomain.DOMAIN);

            var handler = new CrmAdapter(new CrmEntities(crmDb), crmData);
            handler.importEntities();
            assertThat(handler.realm().naturalEntities().items()).isNotEmpty();
            assertThat(handler.realm().legalEntities().items()).isNotEmpty();
            assertThat(handler.realm().employees().items()).isNotEmpty();
            assertThat(handler.realm().contracts().items()).isNotEmpty();
            assertThat(handler.realm().contracts().items().stream().flatMap(o -> o.contractExtensions().stream()).toList()).isNotEmpty();
            assertThat(handler.realm().interactions().items()).isNotEmpty();
            handler.realm().close();

            handler = new CrmAdapter(new CrmEntities(crmDb), crmData);
            assertThat(handler.realm().naturalEntities().items()).isNotEmpty();
            assertThat(handler.realm().legalEntities().items()).isNotEmpty();
            assertThat(handler.realm().employees().items()).isNotEmpty();
            assertThat(handler.realm().contracts().items()).isNotEmpty();
            assertThat(handler.realm().contracts().items().stream().flatMap(o -> o.contractExtensions().stream()).toList()).isNotEmpty();
            assertThat(handler.realm().interactions().items()).isNotEmpty();
            handler.realm().close();
        }
    }

    @Test
    void exportCrmRealmToTsvTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            // given
            var handler = new CrmAdapter(new CrmEntities(store.dbRoot().resolve(CrmBoundedDomain.DOMAIN)), store.dataRoot().resolve(CrmBoundedDomain.DOMAIN));
            handler.importEntities();
            long nrNaturalEntities = handler.realm().naturalEntities().items().size();
            long nrLegalEntities = handler.realm().legalEntities().items().size();
            long nrEmployees = handler.realm().employees().items().size();
            long nrContracts = handler.realm().contracts().items().size();
            long nrContractExtensions = handler.realm().contracts().items().stream().flatMap(o -> o.contractExtensions().stream()).count();
            long nrInteractions = handler.realm().interactions().items().size();

            // when
            handler.exportEntities();
            handler.realm().naturalEntities().deleteAll();
            handler.realm().legalEntities().deleteAll();
            handler.realm().employees().deleteAll();
            handler.realm().contracts().deleteAll();
            handler.realm().interactions().deleteAll();
            handler.realm().close();
            handler = new CrmAdapter(new CrmEntities(store.dbRoot().resolve(CrmBoundedDomain.DOMAIN)), store.dataRoot().resolve(CrmBoundedDomain.DOMAIN));
            handler.importEntities();

            // then
            assertThat(handler.realm().naturalEntities().items().size()).isEqualTo(nrNaturalEntities);
            assertThat(handler.realm().legalEntities().items().size()).isEqualTo(nrLegalEntities);
            assertThat(handler.realm().employees().items().size()).isEqualTo(nrEmployees);
            assertThat(handler.realm().contracts().items().size()).isEqualTo(nrContracts);
            assertThat(handler.realm().contracts().items().stream().flatMap(o -> o.contractExtensions().stream()).count()).isEqualTo(nrContractExtensions);
            assertThat(handler.realm().interactions().items().size()).isEqualTo(nrInteractions);
            handler.realm().close();
        }
    }
}
