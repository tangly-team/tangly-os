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
import java.nio.file.FileSystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.core.TagTypeRegistry;
import org.junit.jupiter.api.Test;

class RealmCrmVcardHdlTest {
    @Test
    void testVcard() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            CrmAndLedgerStore store = new CrmAndLedgerStore(fs);
            store.createCrmAndLedgerRepository();
            CrmHdl crmHdl = new CrmHdl(new CrmEntities(new TagTypeRegistry()));
            crmHdl.importEntities(store.crmRoot());
            CrmVcardHdl handler = new CrmVcardHdl(crmHdl.realm());
            handler.importVCards(store.vcardsRoot());
        }
    }
}
