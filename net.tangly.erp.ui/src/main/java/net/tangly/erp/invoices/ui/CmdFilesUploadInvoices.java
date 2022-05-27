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

package net.tangly.erp.invoices.ui;

import net.tangly.erp.invoices.ports.InvoicesHdl;
import net.tangly.erp.invoices.ports.InvoicesTsvJsonHdl;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import net.tangly.erp.invoices.services.InvoicesHandler;
import net.tangly.erp.invoices.services.InvoicesPort;
import net.tangly.erp.invoices.services.InvoicesRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Command to upload a set of files containing entities. The domain imports the provided entities.
 * The current supported format is TSV and JSON. The entity type is encoded with the filename.
 * The logic takes care of the correct order to process the uploaded files.
 */
public class CmdFilesUploadInvoices extends CmdFilesUpload<InvoicesRealm, InvoicesBusinessLogic, InvoicesHandler, InvoicesPort> {
    CmdFilesUploadInvoices(@NotNull InvoicesBoundedDomain domain) {
        super(domain, TSV_MIME, JSON_MIME);
        registerAllFinishedListener(event -> {
            var handler = new InvoicesTsvJsonHdl(domain.realm());
            Set<String> files = buffer().getFiles();
            if (files.contains(InvoicesHdl.ARTICLES_TSV)) {
                processInputStream(InvoicesHdl.ARTICLES_TSV, handler::importArticles);
            }
            files.stream().filter(o -> o.endsWith(InvoicesHdl.JSON_EXT)).forEach(o -> {
                processInputStream(o, handler::importInvoice);
            });
            close();
        });
    }
}
