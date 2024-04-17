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
import net.tangly.erp.products.ports.ProductsAdapter;
import net.tangly.erp.products.ports.ProductsEntities;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class ProductsPersistenceTest {
    @Test
    void persistProductsRealmLocalTest() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new ProductsAdapter(new ProductsEntities(store.dbRoot().resolve(ProductsBoundedDomain.DOMAIN)), null,
                store.dataRoot().resolve(ProductsBoundedDomain.DOMAIN), store.reportsRoot().resolve(ProductsBoundedDomain.DOMAIN));
            handler.importEntities();
            assertThat(handler.realm().products().items()).isNotEmpty();
            assertThat(handler.realm().assignments().items()).isNotEmpty();
            assertThat(handler.realm().efforts().items()).isNotEmpty();
            handler.realm().close();

            handler = new ProductsAdapter(new ProductsEntities(store.dbRoot().resolve(ProductsBoundedDomain.DOMAIN)), null,
                store.dataRoot().resolve(ProductsBoundedDomain.DOMAIN), store.reportsRoot().resolve(ProductsBoundedDomain.DOMAIN));
            assertThat(handler.realm().products().items()).isNotEmpty();
            assertThat(handler.realm().assignments().items()).isNotEmpty();
            assertThat(handler.realm().efforts().items()).isNotEmpty();
            handler.realm().close();
        }
    }
}
