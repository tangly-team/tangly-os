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

package net.tangly.erp.ledger.ui;

import net.tangly.erp.ledger.ports.LedgerHdl;
import net.tangly.erp.ledger.ports.LedgerTsvHdl;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import net.tangly.erp.ledger.services.LedgerHandler;
import net.tangly.erp.ledger.services.LedgerPort;
import net.tangly.erp.ledger.services.LedgerRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Command to upload a set of files containing entities. The domain imports the provided entities.
 * The current supported format is TSV and JSON. The entity type is encoded with the filename.
 * The logic takes care of the correct order to process the uploaded files.
 */
public class CmdFilesUploadLedger extends CmdFilesUpload<LedgerRealm, LedgerBusinessLogic, LedgerHandler, LedgerPort> {
    public CmdFilesUploadLedger(@NotNull LedgerBoundedDomain domain) {
        super(domain, TSV_MIME);
        registerAllFinishedListener(event -> {
            var handler = new LedgerTsvHdl(domain.realm());
            Set<String> files = buffer().getFiles();
            if (files.contains(LedgerHdl.LEDGER)) {
                processInputStream(LedgerHdl.LEDGER, handler::importChartOfAccounts);
            }
            files.stream().filter(o -> o.endsWith(LedgerHdl.JOURNAL)).forEach(o -> {
                processInputStream(o, handler::importJournal);
            });
            close();
        });
    }
}
