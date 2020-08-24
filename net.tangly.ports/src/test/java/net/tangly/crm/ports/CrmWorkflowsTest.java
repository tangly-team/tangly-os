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

import java.nio.file.Paths;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrmWorkflowsTest {
    private final static String FOLDER = "/Users/Shared/tangly";

    @Test
    @Tag("localTest")
    void readTsvFilesTest() {
        Crm crm = new Crm();
        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.importCrmEntities(Paths.get(FOLDER));
        assertThat(crm.naturalEntities().getAll()).isNotEmpty();
        assertThat(crm.legalEntities().getAll()).isNotEmpty();
        assertThat(crm.employees().getAll()).isNotEmpty();
        assertThat(crm.contracts().getAll()).isNotEmpty();
        assertThat(crm.products().getAll()).isNotEmpty();
        assertThat(crm.subjects().getAll()).isNotEmpty();
    }

    @Test
    @Tag("localTest")
    void writeTsvFilesTest() {
        Crm crm = new Crm();
        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.importCrmEntities(Paths.get(FOLDER));
        crmWorkflows.exportCrmEntities(Paths.get(FOLDER));

    }
}
