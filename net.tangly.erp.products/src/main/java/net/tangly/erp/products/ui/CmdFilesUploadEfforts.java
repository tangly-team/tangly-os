/*
 * Copyright 2023-2024 Marcel Baumann
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

package net.tangly.erp.products.ui;

import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erp.products.services.ProductsPort;
import net.tangly.erp.products.services.ProductsRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * The command defines how to upload entities provided as a set of YAML files onto the domain.
 * Overwrite the dialog definition to add the checkbox for overwrite existing efforts.
 */
public class CmdFilesUploadEfforts extends CmdFilesUpload<ProductsRealm, ProductsBusinessLogic, ProductsPort> {
    CmdFilesUploadEfforts(@NotNull ProductsBoundedDomain domain) {
        super(domain, CmdFilesUpload.TSV_MIME, CmdFilesUpload.JSON_MIME);
        registerAllFinishedListener(event -> {
            Set<String> files = buffer().getFiles();
            files.forEach(o -> domain.port().importEfforts(domain, new BufferedReader(new InputStreamReader(buffer().getInputStream(o))), o, true));
            close();
        });
    }
}
