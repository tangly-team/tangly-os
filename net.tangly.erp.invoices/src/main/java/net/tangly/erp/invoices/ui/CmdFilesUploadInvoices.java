/*
 * Copyright 2022-2024 Marcel Baumann
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

package net.tangly.erp.invoices.ui;

import net.tangly.core.domain.Operation;
import net.tangly.core.events.EntityChangedInternalEvent;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesTsvJsonHdl;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import net.tangly.erp.invoices.services.InvoicesPort;
import net.tangly.erp.invoices.services.InvoicesRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * The command defines how to upload entities provided as a set of TSV files onto the domain.
 */
public class CmdFilesUploadInvoices extends CmdFilesUpload<InvoicesRealm, InvoicesBusinessLogic, InvoicesPort> {
    CmdFilesUploadInvoices(@NotNull InvoicesBoundedDomain domain) {
        super(domain, CmdFilesUpload.TSV_MIME, CmdFilesUpload.JSON_MIME);
        registerAllFinishedListener(event -> {
            var handler = new InvoicesTsvJsonHdl(domain.realm());
            Set<String> files = buffer().getFiles();
            files.stream().filter(o -> o.endsWith(InvoicesAdapter.JSON_EXT)).forEach(o -> processInputStream(domain, o, handler::importInvoice));
            domain().internalChannel().submit(new EntityChangedInternalEvent(domain().name(), Invoice.class.getSimpleName(), Operation.CREATE));
            close();
        });
    }
}
