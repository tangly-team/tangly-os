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

package net.tangly.erp.crm;

import net.tangly.erp.crm.ports.CrmCsvHdl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvImportTest {
    @Test
    void testCsvCrm() throws IOException, URISyntaxException {
        CrmCsvHdl handler = new CrmCsvHdl();
        Path path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/crm/naturalEntities.csv").toURI());
        List<NaturalEntity> naturalEntities = handler.importNaturalEntities(path);
        assertThat(naturalEntities.size()).isEqualTo(5);

        path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/crm/legalEntities.csv").toURI());
        List<LegalEntity> legalEntities = handler.importLegalEntities(path);
        assertThat(legalEntities.size()).isEqualTo(2);

        path = Paths.get(getClass().getClassLoader().getResource("net/tangly/erp/crm/employees.csv").toURI());
        List<Employee> employees = handler.importEmployees(path);
        assertThat(employees.size()).isEqualTo(4);

    }

    @Test
    @Tag("LocalTest")
    void testCsvNaturalEntityImport() throws IOException {
        CrmCsvHdl handler = new CrmCsvHdl();

        List<NaturalEntity> naturalEntities = handler.importNaturalEntities(Paths.get("/Users/Shared/tmp/naturalEntities.csv"));
        List<LegalEntity> legalEntities = handler.importLegalEntities(Paths.get("/Users/Shared/tmp/legalEntities.csv"));
        List<Employee> employees = handler.importEmployees(Paths.get("/Users/Shared/tmp/employees.csv"));

    }
}
