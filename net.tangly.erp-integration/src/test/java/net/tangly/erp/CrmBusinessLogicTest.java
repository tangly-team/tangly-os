/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp;

import net.tangly.core.TypeRegistry;
import net.tangly.core.crm.CrmTags;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
class CrmBusinessLogicTest {
    @Test
    void testCrmTags() {
        var registry = new TypeRegistry();
        CrmTags.registerTags(registry);
        assertThat(registry.namespaces().size()).isEqualTo(2);
        assertThat(registry.namespaces()).contains(CrmTags.CRM, CrmTags.GEO);
        assertThat(registry.tagNamesForNamespace(CrmTags.GEO)).contains(CrmTags.LATITUDE, CrmTags.LONGITUDE, CrmTags.ALTITUDE, CrmTags.PLUSCODE);
        assertThat(registry.tagNamesForNamespace(CrmTags.CRM).isEmpty()).isFalse();
    }
}
