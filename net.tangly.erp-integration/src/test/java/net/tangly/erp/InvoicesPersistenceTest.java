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
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicesPersistenceTest {
    @Test
    void persistInvoicesRealmToDbTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new InvoicesAdapter(new InvoicesEntities(store.dbRoot().resolve(InvoicesBoundedDomain.DOMAIN)), store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN));
            handler.importEntities();
            assertThat(handler.realm().articles().items()).isNotEmpty();
            assertThat(handler.realm().invoices().items()).isNotEmpty();
            handler.realm().close();

            handler = new InvoicesAdapter(new InvoicesEntities(store.dbRoot().resolve(InvoicesBoundedDomain.DOMAIN)), store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN));
            assertThat(handler.realm().articles().items()).isNotEmpty();
            assertThat(handler.realm().invoices().items()).isNotEmpty();
            handler.realm().close();
        }
    }

    @Test
    void exportInvoicesRealmToTsvTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new InvoicesAdapter(new InvoicesEntities(store.dbRoot().resolve(InvoicesBoundedDomain.DOMAIN)), store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN));
            handler.importEntities();
            long nrArticles = handler.realm().articles().items().size();
            long nrInvoices = handler.realm().invoices().items().size();
            handler.exportEntities();
            handler.clearEntities();
            handler.realm().close();

            handler = new InvoicesAdapter(new InvoicesEntities(store.dbRoot().resolve(InvoicesBoundedDomain.DOMAIN)), store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN));
            handler.importEntities();
            assertThat(handler.realm().articles().items().size()).isEqualTo(nrArticles);
            assertThat(handler.realm().invoices().items().size()).isEqualTo(nrInvoices);
            handler.realm().close();
        }
    }
}
