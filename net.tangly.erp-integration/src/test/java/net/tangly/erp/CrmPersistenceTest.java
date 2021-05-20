/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp;

import java.nio.file.FileSystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.erpr.crm.ports.CrmEntities;
import net.tangly.erpr.crm.ports.CrmHdl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrmPersistenceTest {
    @Test
    @Disabled
    void persistCrmRealLocalTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            var handler = new CrmHdl(new CrmEntities(store.crmRoot()), store.crmRoot());
            handler.importEntities();
            assertThat(handler.realm().naturalEntities().items().isEmpty()).isFalse();
            assertThat(handler.realm().legalEntities().items().isEmpty()).isFalse();
            assertThat(handler.realm().employees().items().isEmpty()).isFalse();
            assertThat(handler.realm().contracts().items().isEmpty()).isFalse();
            assertThat(handler.realm().interactions().items().isEmpty()).isFalse();
            handler.realm().close();

            handler = new CrmHdl(new CrmEntities(store.crmRoot()), store.crmRoot());
            assertThat(handler.realm().naturalEntities().items().isEmpty()).isFalse();
            assertThat(handler.realm().legalEntities().items().isEmpty()).isFalse();
            assertThat(handler.realm().employees().items().isEmpty()).isFalse();
            assertThat(handler.realm().contracts().items().isEmpty()).isFalse();
            assertThat(handler.realm().interactions().items().isEmpty()).isFalse();
            handler.realm().close();
        }
    }
}
