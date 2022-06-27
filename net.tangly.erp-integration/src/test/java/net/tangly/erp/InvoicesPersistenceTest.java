/*
 * Copyright 2006-2022 Marcel Baumann
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
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.ports.InvoicesHdl;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicesPersistenceTest {
    @Test
    void persistProductsRealmLocalTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new InvoicesHdl(new InvoicesEntities(store.invoicesRoot()), store.invoicesRoot());
            handler.importEntities();
            assertThat(handler.realm().invoices().items().isEmpty()).isFalse();
            handler.realm().close();

            handler = new InvoicesHdl(new InvoicesEntities(store.invoicesRoot()), store.invoicesRoot());
            assertThat(handler.realm().invoices().items().isEmpty()).isFalse();
            handler.realm().close();
        }
    }
}
