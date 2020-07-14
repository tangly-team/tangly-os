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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrmTsvHdlTest {
    static final String PACKAGE_NAME = "net/tangly/crm/ports/";

    @Test
    void testCsvCrm() throws IOException, URISyntaxException {
        Crm crm = new Crm();
        CrmTsvHdl handler = new CrmTsvHdl(crm);

        handler.importNaturalEntities(fromResource(PACKAGE_NAME + "natural-entities.tsv"));
        NaturalEntity naturalEntity = crm.naturalEntities().getAll().get(0);
        assertThat(crm.naturalEntities().getAll().size()).isEqualTo(6);
        assertThat(naturalEntity.oid()).isEqualTo(1);
        assertThat(naturalEntity.id()).isEqualTo("jd-01");
        assertThat(naturalEntity.address(CrmTags.CRM_ADDRESS_HOME)).isNotNull();
        assertThat(naturalEntity.email(CrmTags.HOME)).isNotNull();
        assertThat(naturalEntity.phoneNr(CrmTags.HOME)).isNotNull();
        assertThat(naturalEntity.site(CrmTags.HOME)).isNotNull();

        handler.importLegalEntities(fromResource(PACKAGE_NAME + CrmWorkflows.LEGAL_ENTITIES_TSV));
        LegalEntity legalEntity = crm.legalEntities().getAll().get(0);
        assertThat(crm.legalEntities().getAll().size()).isEqualTo(3);
        assertThat(legalEntity.oid()).isEqualTo(100);
        assertThat(legalEntity.id()).isEqualTo("UNKNOWN-100");
        assertThat(legalEntity.name()).isEqualTo("hope llc");

        handler.importEmployees(fromResource(PACKAGE_NAME + CrmWorkflows.EMPLOYEES_TSV));
        assertThat(crm.employees().getAll().size()).isEqualTo(5);

        handler.importContracts(fromResource(PACKAGE_NAME + CrmWorkflows.CONTRACTS_TSV));
        assertThat(crm.contracts().getAll().size()).isNotEqualTo(0);
    }

    private Path fromResource(String resource) {
        return Paths.get(getClass().getClassLoader().getResource(resource).getPath());
    }
}
