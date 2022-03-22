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

import java.io.IOException;
import java.nio.file.FileSystem;

import com.google.common.jimfs.Jimfs;
import net.tangly.erp.products.ports.EffortReportEngine;
import net.tangly.erp.products.ports.ProductsEntities;
import net.tangly.erp.products.ports.ProductsHdl;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductsPortTest {
    long ASSIGNMENT_OID = 400;

    @Test
    void testWorkReport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();
            var entities = new ProductsEntities();
            var logic = new ProductsBusinessLogic(entities);
            var handler = new ProductsHdl(entities, store.productsRoot());
            handler.importEntities();
            EffortReportEngine reporter = new EffortReportEngine(logic);
//            reporter.create(Provider.findByOid(entities.assignments(), ASSIGNMENT_OID).orElseThrow(), LocalDate.of(2020, Month.JANUARY, 1),
//                LocalDate.of(2020, Month.JANUARY, 31), store.reportsRoot());
            assertThat(reporter).isNotNull();
        }
    }
}
