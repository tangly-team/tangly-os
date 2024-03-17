/*
 * Copyright 2006-2022 Marcel Baumann
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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.erp.crm.ports.CrmEntities;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmVcardHdl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CrmRealmVcardHdlTest {
    @Test
    void testVcard() throws IOException {
        var realm = new CrmEntities();
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var crmHdl = new CrmAdapter(realm, store.crmRoot());
            crmHdl.importEntities();
            var handler = new CrmVcardHdl(crmHdl.realm());
            handler.importVCards(store.vcardsRoot());
        }
        assertThat(realm.naturalEntities().items().stream().filter(o -> o.oid() == 6).findAny().orElseThrow().hasPhoto()).isTrue();
    }
}
