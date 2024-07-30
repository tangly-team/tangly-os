/*
 * Copyright 2022-2024 Marcel Baumann
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
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerPersistenceTest {
    @Test
    void persistLedgerRealmToDbTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var ledgerDb = store.dbRoot().resolve(LedgerBoundedDomain.DOMAIN);
            var ledgerData = store.dataRoot().resolve(LedgerBoundedDomain.DOMAIN);
            var ledgerReport = store.reportsRoot().resolve(LedgerBoundedDomain.DOMAIN);
            var handler = new LedgerAdapter(new LedgerEntities(ledgerDb), new TypeRegistry(), ledgerData, ledgerReport);
            handler.importEntities(store);
            assertThat(handler.realm().accounts().items()).isNotEmpty();
            assertThat(handler.realm().transactions().items()).isNotEmpty();
            handler.realm().close();

            handler = new LedgerAdapter(new LedgerEntities(ledgerDb), new TypeRegistry(), ledgerData, ledgerReport);
            assertThat(handler.realm().accounts().items()).isNotEmpty();
            assertThat(handler.realm().transactions().items()).isNotEmpty();
            handler.realm().close();
        }
    }

    @Test
    void exportProductsRealmToTsvTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var ledgerDb = store.dbRoot().resolve(LedgerBoundedDomain.DOMAIN);
            var ledgerData = store.dataRoot().resolve(LedgerBoundedDomain.DOMAIN);
            var ledgerReport = store.reportsRoot().resolve(LedgerBoundedDomain.DOMAIN);
            var handler = new LedgerAdapter(new LedgerEntities(ledgerDb), new TypeRegistry(), ledgerData, ledgerReport);
            handler.importEntities(store);
            long nrAccounts = handler.realm().accounts().items().size();
            long nrTransactions = handler.realm().transactions().items().size();
            handler.exportEntities(store);
            handler.clearEntities(store);
            handler.realm().close();

            handler = new LedgerAdapter(new LedgerEntities(ledgerDb), new TypeRegistry(), ledgerData, ledgerReport);
            handler.importEntities(store);
            assertThat(handler.realm().accounts().items().size()).isEqualTo(nrAccounts);
            assertThat(handler.realm().transactions().items().size()).isEqualTo(nrTransactions);
            handler.realm().close();
        }
    }
}
