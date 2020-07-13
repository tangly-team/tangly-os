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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrmTsvHdlTest {
    static final String PACKAGE_NAME = "net/tangly/crm/ports/";

    @Test
    void testCsvCrm() throws IOException, URISyntaxException {
        Crm crm = new Crm();
        CrmTsvHdl handler = new CrmTsvHdl(crm);

        handler.importNaturalEntities(fromResource(PACKAGE_NAME + "natural-entities.tsv"));
        assertThat(crm.naturalEntities().size()).isEqualTo(6);
        assertThat(crm.naturalEntities().get(0).oid()).isEqualTo(1);
        assertThat(crm.naturalEntities().get(0).id()).isEqualTo("jd-01");
        assertThat(crm.naturalEntities().get(0).address(CrmTags.CRM_ADDRESS_HOME)).isNotNull();
        assertThat(crm.naturalEntities().get(0).email(CrmTags.HOME)).isNotNull();
        assertThat(crm.naturalEntities().get(0).phoneNr(CrmTags.HOME)).isNotNull();
        assertThat(crm.naturalEntities().get(0).site(CrmTags.HOME)).isNotNull();

        handler.importLegalEntities(fromResource(PACKAGE_NAME + CrmWorkflows.LEGAL_ENTITIES_TSV));
        assertThat(crm.legalEntities().size()).isEqualTo(3);
        assertThat(crm.legalEntities().get(0).oid()).isEqualTo(100);
        assertThat(crm.legalEntities().get(0).id()).isEqualTo("UNKNOWN-100");
        assertThat(crm.legalEntities().get(0).name()).isEqualTo("hope llc");

        handler.importEmployees(fromResource(PACKAGE_NAME + CrmWorkflows.EMPLOYEES_TSV));
        assertThat(crm.employees().size()).isEqualTo(5);

        handler.importContracts(fromResource(PACKAGE_NAME + CrmWorkflows.CONTRACTS_TSV));
        assertThat(crm.contracts().size()).isNotEqualTo(0);
    }

    private Path fromResource(String resource) {
        return Paths.get(getClass().getClassLoader().getResource(resource).getPath());
    }
}
