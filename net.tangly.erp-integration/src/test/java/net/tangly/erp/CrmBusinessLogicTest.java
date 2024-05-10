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
import net.tangly.core.TypeRegistry;
import net.tangly.core.crm.CrmTags;
import net.tangly.erp.crm.domain.InteractionCode;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmEntities;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system.
 */
class CrmBusinessLogicTest {
    @Test
    void testCrmTags() {
        var registry = new TypeRegistry();
        CrmTags.registerTags(registry);
        assertThat(registry.namespaces()).hasSize(2);
        assertThat(registry.namespaces()).contains(CrmTags.CRM, CrmTags.GEO);
        assertThat(registry.tagNamesForNamespace(CrmTags.GEO)).contains(CrmTags.LATITUDE, CrmTags.LONGITUDE, CrmTags.ALTITUDE, CrmTags.PLUSCODE);
        assertThat(registry.tagNamesForNamespace(CrmTags.CRM)).isNotEmpty();
    }

    @Test
    void testFunnel() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var handler = new CrmAdapter(new CrmEntities(store.dbRoot().resolve(CrmBoundedDomain.DOMAIN)), store.dataRoot().resolve(CrmBoundedDomain.DOMAIN));
            var logic = new CrmBusinessLogic(handler.realm());
            handler.importEntities();

            assertThat(logic.funnel(InteractionCode.prospect, LocalDate.of(2015, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31))).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(logic.funnel(InteractionCode.lead, LocalDate.of(2015, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31))).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(logic.funnel(InteractionCode.ordered, LocalDate.of(2015, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31))).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(logic.funnel(InteractionCode.completed, LocalDate.of(2015, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31))).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(logic.funnel(InteractionCode.lost, LocalDate.of(2015, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31))).isGreaterThanOrEqualTo(BigDecimal.ZERO);

        }
    }
}
