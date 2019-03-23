/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.crm.models;

import net.tangly.erp.crm.models.apps.Crm;
import net.tangly.erp.crm.models.ports.CrmCsvHdl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvImportTest {
    @Test
    void testCsvCrm() throws IOException, URISyntaxException {
        Crm crm = new Crm();
        CrmTags.registerTags(crm.tagTypeRegistry());
        CrmCsvHdl handler = new CrmCsvHdl(crm);

        Path path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/crm/models/naturalEntities.csv").toURI());
        handler.importNaturalEntities(path);
        assertThat(crm.naturalEntities().size()).isEqualTo(5);

        assertThat(crm.naturalEntities().get(0).oid()).isEqualTo(1);
        assertThat(crm.naturalEntities().get(0).id()).isNull();
        assertThat(crm.naturalEntities().get(0).address(CrmTags.CRM_ADDRESS_HOME)).isNotNull();
        assertThat(crm.naturalEntities().get(0).findEmail(CrmTags.HOME)).isNotNull();
        assertThat(crm.naturalEntities().get(0).findPhoneNr(CrmTags.HOME)).isNotNull();
        // assertThat(crm.naturalEntities().getAt(0).findSite(CrmTags.HOME)).isNotNull();

        path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/crm/models/legalEntities.csv").toURI());
        handler.importLegalEntities(path);
        assertThat(crm.legalEntities().size()).isEqualTo(2);

        path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/crm/models/employees.csv").toURI());
        handler.importEmployees(path);
        assertThat(crm.employees().size()).isEqualTo(4);

        path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/crm/models/contracts.csv").toURI());
        handler.importContracts(path);
        assertThat(crm.contracts().size()).isEqualTo(5);
    }

    @Test
    @Tag("localTest")
    void testCsvNaturalEntityImport() throws IOException {
        Crm crm = new Crm();
        CrmTags.registerTags(crm.tagTypeRegistry());
        CrmCsvHdl handler = new CrmCsvHdl(crm);

        handler.importNaturalEntities(Paths.get("/Users/Shared/tmp/naturalEntities.csv"));
        handler.importLegalEntities(Paths.get("/Users/Shared/tmp/legalEntities.csv"));
        handler.importEmployees(Paths.get("/Users/Shared/tmp/employees.csv"));

    }
}
