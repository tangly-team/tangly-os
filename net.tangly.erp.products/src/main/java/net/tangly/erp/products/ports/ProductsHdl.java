/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.products.ports;

import net.tangly.core.domain.Handler;
import net.tangly.erp.products.services.ProductsHandler;
import net.tangly.erp.products.services.ProductsRealm;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ProductsHdl implements ProductsHandler {
    public static final String PRODUCTS_TSV = "products.tsv";
    public static final String ASSIGNMENTS_TSV = "assignments.tsv";
    public static final String EFFORTS_TSV = "efforts.tsv";

    private final ProductsRealm realm;
    private final Path folder;

    public ProductsHdl(@NotNull ProductsRealm realm, @NotNull Path folder) {
        this.realm = realm;
        this.folder = folder;
    }

    @Override
    public ProductsRealm realm() {
        return realm;
    }

    @Override
    public void importEntities() {
        var handler = new ProductsTsvHdl(realm());
        Handler.importEntities(folder, PRODUCTS_TSV, handler::importProducts);
        Handler.importEntities(folder, ASSIGNMENTS_TSV, handler::importAssignments);
        Handler.importEntities(folder, EFFORTS_TSV, handler::importEfforts);
    }

    @Override
    public void exportEntities() {
        var handler = new ProductsTsvHdl(realm());
        handler.exportProducts(folder.resolve(PRODUCTS_TSV));
        handler.exportAssignments(folder.resolve(ASSIGNMENTS_TSV));
        handler.exportEfforts(folder.resolve(EFFORTS_TSV));
    }

    @Override
    public void clearEntities() {
        realm().efforts().deleteAll();
        realm().assignments().deleteAll();
        realm().products().deleteAll();
    }
}
