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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.products.ProductsRealm;
import net.tangly.core.domain.Realm;
import net.tangly.products.ports.ProductsEntities;
import net.tangly.products.ports.ProductsHdl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class ProductsHdlTest {
    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            var handler = new ProductsHdl(new ProductsEntities(), store.productsRoot());
            handler.importEntities();
            verifyProducts(handler.realm());
            verifyAssignements(handler.realm());
            verifyEfforts(handler.realm());
            handler.exportEntities();

            handler = new ProductsHdl(new ProductsEntities(), store.productsRoot());
            handler.importEntities();
            verifyProducts(handler.realm());
            verifyAssignements(handler.realm());
            verifyEfforts(handler.realm());
        }
    }

    private void verifyProducts(@NotNull ProductsRealm realm) {
        assertThat(realm.products().items().isEmpty()).isFalse();
        Realm.checkEntities(realm.products());
    }

    private void verifyAssignements(@NotNull ProductsRealm realm) {
        assertThat(realm.assignments().items().isEmpty()).isFalse();
        Realm.checkEntities(realm.assignments());
    }

    private void verifyEfforts(@NotNull ProductsRealm realm) {
        assertThat(realm.efforts().items().isEmpty()).isFalse();
        realm.efforts().items().forEach(o -> assertThat(o.check()).isTrue());
    }
}
