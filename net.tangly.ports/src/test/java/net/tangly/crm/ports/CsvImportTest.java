/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.crm.ports;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.tangly.apps.Crm;
import net.tangly.bus.crm.CrmTags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvImportTest {
    static final String PACKAGE_NAME = "net/tangly/crm/ports/";

    @Test
    void testCsvCrm() throws IOException, URISyntaxException {
        Crm crm = new Crm();
        CrmTags.registerTags(crm.tagTypeRegistry());
        CrmCsvHdl handler = new CrmCsvHdl(crm);

        handler.importNaturalEntities(fromResource(PACKAGE_NAME + "naturalEntities.tsv"));
        assertThat(crm.naturalEntities().size()).isEqualTo(6);

        assertThat(crm.naturalEntities().get(0).oid()).isEqualTo(1);
        assertThat(crm.naturalEntities().get(0).id()).isNull();
        assertThat(crm.naturalEntities().get(0).address(CrmTags.CRM_ADDRESS_HOME)).isNotNull();
        assertThat(crm.naturalEntities().get(0).findEmail(CrmTags.HOME)).isNotNull();
        assertThat(crm.naturalEntities().get(0).findPhoneNr(CrmTags.HOME)).isNotNull();
        // assertThat(crm.naturalEntities().getAt(0).findSite(CrmTags.HOME)).isNotNull();

        handler.importLegalEntities(fromResource(PACKAGE_NAME + "legalEntities.tsv"));
        assertThat(crm.legalEntities().size()).isEqualTo(3);

        handler.importEmployees(fromResource(PACKAGE_NAME + "employees.tsv"));
        assertThat(crm.employees().size()).isEqualTo(5);

        handler.importContracts(fromResource(PACKAGE_NAME + "contracts.tsv"));
        assertThat(crm.contracts().size()).isEqualTo(5);
    }

    @Test
    @Tag("localTest")
    void testCsvNaturalEntityImport() throws IOException {
        Crm crm = new Crm();
        CrmTags.registerTags(crm.tagTypeRegistry());
        CrmCsvHdl handler = new CrmCsvHdl(crm);

        handler.importNaturalEntities(fromResource(PACKAGE_NAME + "naturalEntities.tsv"));
        handler.importLegalEntities(fromResource(PACKAGE_NAME + "legalEntities.tsv"));
        handler.importEmployees(fromResource(PACKAGE_NAME + "employees.tsv"));

    }

    private Path fromResource(String resource) {
        return Paths.get(getClass().getClassLoader().getResource(resource).getPath());
    }
}
