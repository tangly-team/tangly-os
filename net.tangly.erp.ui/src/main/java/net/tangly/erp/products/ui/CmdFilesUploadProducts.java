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

package net.tangly.erp.products.ui;

import net.tangly.erp.products.ports.ProductsHdl;
import net.tangly.erp.products.ports.ProductsTsvHdl;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erp.products.services.ProductsHandler;
import net.tangly.erp.products.services.ProductsPort;
import net.tangly.erp.products.services.ProductsRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Command to upload a set of files containing entities. The domain imports the provided entities.
 * The current supported format is TSV and JSON. The entity type is encoded with the filename.
 * The logic takes care of the correct order to process the uploaded files.
 */
public class CmdFilesUploadProducts extends CmdFilesUpload<ProductsRealm, ProductsBusinessLogic, ProductsHandler, ProductsPort> {
    CmdFilesUploadProducts(@NotNull ProductsBoundedDomain domain) {
        super(domain, TSV_MIME);
        registerAllFinishedListener(
            (event -> {
                var handler = new ProductsTsvHdl(domain.realm());
                Set<String> files = buffer().getFiles();
                if (files.contains(ProductsHdl.PRODUCTS_TSV)) {
                    handler.importProducts(createReader(ProductsHdl.PRODUCTS_TSV), ProductsHdl.PRODUCTS_TSV);
                }
                if (files.contains(ProductsHdl.ASSIGNMENTS_TSV)) {
                    handler.importAssignments(createReader(ProductsHdl.ASSIGNMENTS_TSV), ProductsHdl.ASSIGNMENTS_TSV);
                }
                if (files.contains(ProductsHdl.EFFORTS_TSV)) {
                    handler.importEfforts(createReader(ProductsHdl.EFFORTS_TSV), ProductsHdl.EFFORTS_TSV);
                }
                close();
            }));
    }
}

