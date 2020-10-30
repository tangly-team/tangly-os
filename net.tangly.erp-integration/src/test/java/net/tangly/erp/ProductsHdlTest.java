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

package net.tangly.erp;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.products.ports.ProductsEntities;
import net.tangly.products.ports.ProductsHdl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ProductsHdlTest {
    @Test
    @Tag("localTest")
    void testCompanyTsvCrm() {
        ProductsHdl productsHdl = new ProductsHdl(new ProductsEntities(new TagTypeRegistry()), Path.of("/Users/Shared/tangly/", "products"));
        productsHdl.importEntities();
    }

    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ErpStore store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            ProductsHdl productsHdl = new ProductsHdl(new ProductsEntities(new TagTypeRegistry()), store.productsRoot());
            productsHdl.importEntities();

            // TODO test
            productsHdl.exportEntities();

            // TODO test
        }
    }
}
