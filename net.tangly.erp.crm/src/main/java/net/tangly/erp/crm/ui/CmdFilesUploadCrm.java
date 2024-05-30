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

package net.tangly.erp.crm.ui;

import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmTsvHdl;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.crm.services.CrmRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * The command defines how to upload entities provided as a set of TSV files onto the domain.
 */
public class CmdFilesUploadCrm extends CmdFilesUpload<CrmRealm, CrmBusinessLogic, CrmAdapter> {
    CmdFilesUploadCrm(@NotNull CrmBoundedDomain domain) {
        super(domain, CmdFilesUpload.TSV_MIME);
        registerAllFinishedListener((event -> {
            var handler = new CrmTsvHdl(domain.realm());
            Set<String> files = buffer().getFiles();
            if (files.contains(CrmAdapter.LEADS_TSV)) {
                processInputStream(CrmAdapter.LEADS_TSV, handler::importLeads);
            }
            if (files.contains(CrmAdapter.NATURAL_ENTITIES_TSV)) {
                processInputStream(CrmAdapter.NATURAL_ENTITIES_TSV, handler::importNaturalEntities);
            }
            if (files.contains(CrmAdapter.LEGAL_ENTITIES_TSV)) {
                processInputStream(CrmAdapter.LEGAL_ENTITIES_TSV, handler::importLegalEntities);
            }
            if (files.contains(CrmAdapter.EMPLOYEES_TSV)) {
                processInputStream(CrmAdapter.EMPLOYEES_TSV, handler::importEmployees);
            }
            if (files.contains(CrmAdapter.CONTRACTS_TSV)) {
                processInputStream(CrmAdapter.CONTRACTS_TSV, handler::importContracts);
            }
            if (files.contains(CrmAdapter.INTERACTIONS_TSV)) {
                processInputStream(CrmAdapter.INTERACTIONS_TSV, handler::importInteractions);
            }
            if (files.contains(CrmAdapter.ACTIVITIES_TSV)) {
                processInputStream(CrmAdapter.ACTIVITIES_TSV, handler::importActivities);
            }
            if (files.contains(CrmAdapter.COMMENTS_TSV)) {
                processInputStream(CrmAdapter.COMMENTS_TSV, handler::importComments);
            }
            close();
        }));
    }
}
