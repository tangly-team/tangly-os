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

import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesTsvJsonHdl;
import net.tangly.erp.products.services.*;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * The command defines how to upload entities provided as a set of YAML files onto the domain.
 */
public class CmdFilesUploadefforts extends CmdFilesUpload<ProductsRealm, ProductsBusinessLogic, ProductsPort> {
    CmdFilesUploadefforts(@NotNull ProductsBoundedDomain domain) {
        super(domain, TSV_MIME, JSON_MIME);
        registerAllFinishedListener(event -> {
            close();
        });
    }
}