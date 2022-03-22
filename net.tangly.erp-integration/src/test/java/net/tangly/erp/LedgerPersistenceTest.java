/*
 * Copyright 2022-2022 Marcel Baumann
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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.ports.LedgerHdl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
public class LedgerPersistenceTest {
    @Test
    void persistLedgerRealmLocalTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new LedgerHdl(new LedgerEntities(store.ledgerRoot()), store.ledgerRoot());
            handler.importEntities();
            assertThat(handler.realm().accounts().items().isEmpty()).isFalse();
            assertThat(handler.realm().transactions().items().isEmpty()).isFalse();
            handler.realm().close();

            handler = new LedgerHdl(new LedgerEntities(store.invoicesRoot()), store.ledgerRoot());
            assertThat(handler.realm().accounts().items().isEmpty()).isFalse();
            assertThat(handler.realm().transactions().items().isEmpty()).isFalse();
            handler.realm().close();
        }
    }
}
