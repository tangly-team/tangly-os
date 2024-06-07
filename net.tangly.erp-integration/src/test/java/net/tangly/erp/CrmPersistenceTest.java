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
import net.tangly.core.providers.Provider;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.Interaction;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.domain.NaturalEntity;
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
            handler.importEntities(store);
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
            final String NATURAL_ENTITY_ID = "756.5149.8825.64";
            final String LEGAL_ENTITY_ID = "CHE-357.875.339";
            final String CONTRACT_ID = "STG-2019";
            final long INTERACTION_OID = 600;
            var store = new ErpStore(fs);
            store.createRepository();

            // given
            var handler = new CrmAdapter(new CrmEntities(store.dbRoot().resolve(CrmBoundedDomain.DOMAIN)), store.dataRoot().resolve(CrmBoundedDomain.DOMAIN));
            handler.importEntities(store);
            long nrNaturalEntities = handler.realm().naturalEntities().items().size();
            long nrLegalEntities = handler.realm().legalEntities().items().size();
            long nrEmployees = handler.realm().employees().items().size();
            long nrContracts = handler.realm().contracts().items().size();
            long nrContractExtensions = handler.realm().contracts().items().stream().mapToLong(o -> o.contractExtensions().size()).sum();
            long nrInteractions = handler.realm().interactions().items().size();

            NaturalEntity naturalEntity = Provider.findById(handler.realm().naturalEntities(), NATURAL_ENTITY_ID).orElseThrow();
            LegalEntity legalEntity = Provider.findById(handler.realm().legalEntities(), LEGAL_ENTITY_ID).orElseThrow();
            Contract contract = Provider.findById(handler.realm().contracts(), CONTRACT_ID).orElseThrow();
            Interaction interaction = Provider.findByOid(handler.realm().interactions(), INTERACTION_OID).orElseThrow();

            // when
            handler.exportEntities(store);
            handler.clearEntities();
            handler.realm().close();
            handler = new CrmAdapter(new CrmEntities(store.dbRoot().resolve(CrmBoundedDomain.DOMAIN)), store.dataRoot().resolve(CrmBoundedDomain.DOMAIN));
            handler.importEntities(store);

            // then
            assertThat(handler.realm().naturalEntities().items().size()).isEqualTo(nrNaturalEntities);
            assertThat(handler.realm().legalEntities().items().size()).isEqualTo(nrLegalEntities);
            assertThat(handler.realm().employees().items().size()).isEqualTo(nrEmployees);
            assertThat(handler.realm().contracts().items().size()).isEqualTo(nrContracts);
            assertThat(handler.realm().contracts().items().stream().mapToLong(o -> o.contractExtensions().size()).sum()).isEqualTo(nrContractExtensions);
            assertThat(handler.realm().interactions().items().size()).isEqualTo(nrInteractions);

            assertThat(naturalEntity).isEqualTo(Provider.findById(handler.realm().naturalEntities(), NATURAL_ENTITY_ID).orElseThrow());
            assertThat(legalEntity).isEqualTo(Provider.findById(handler.realm().legalEntities(), LEGAL_ENTITY_ID).orElseThrow());
            assertThat(contract).isEqualTo(Provider.findById(handler.realm().contracts(), CONTRACT_ID).orElseThrow());
            assertThat(interaction).isEqualTo(Provider.findByOid(handler.realm().interactions(), INTERACTION_OID).orElseThrow());
            handler.realm().close();
        }
    }
}
