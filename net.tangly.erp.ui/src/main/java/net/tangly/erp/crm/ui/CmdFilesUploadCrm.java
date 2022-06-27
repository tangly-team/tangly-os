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

package net.tangly.erp.crm.ui;

import net.tangly.erp.crm.ports.CrmHdl;
import net.tangly.erp.crm.ports.CrmTsvHdl;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.crm.services.CrmHandler;
import net.tangly.erp.crm.services.CrmPort;
import net.tangly.erp.crm.services.CrmRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * The command defines how to upload entities provided as a set of TSV files onto the domain.
 */
public class CmdFilesUploadCrm extends CmdFilesUpload<CrmRealm, CrmBusinessLogic, CrmHandler, CrmPort> {
    CmdFilesUploadCrm(@NotNull CrmBoundedDomain domain) {
        super(domain, TSV_MIME);
        registerAllFinishedListener((event -> {
            var handler = new CrmTsvHdl(domain.realm());
            Set<String> files = buffer().getFiles();
            if (files.contains(CrmHdl.LEADS_TSV)) {
                processInputStream(CrmHdl.LEADS_TSV, handler::importLeads);
            }
            if (files.contains(CrmHdl.NATURAL_ENTITIES_TSV)) {
                processInputStream(CrmHdl.NATURAL_ENTITIES_TSV, handler::importNaturalEntities);
            }
            if (files.contains(CrmHdl.LEGAL_ENTITIES_TSV)) {
                processInputStream(CrmHdl.LEGAL_ENTITIES_TSV, handler::importLegalEntities);
            }
            if (files.contains(CrmHdl.EMPLOYEES_TSV)) {
                processInputStream(CrmHdl.EMPLOYEES_TSV, handler::importEmployees);
            }
            if (files.contains(CrmHdl.CONTRACTS_TSV)) {
                processInputStream(CrmHdl.CONTRACTS_TSV, handler::importContracts);
            }
            if (files.contains(CrmHdl.INTERACTIONS_TSV)) {
                processInputStream(CrmHdl.INTERACTIONS_TSV, handler::importInteractions);
            }
            if (files.contains(CrmHdl.ACTIVITIES_TSV)) {
                processInputStream(CrmHdl.ACTIVITIES_TSV, handler::importActivities);
            }
            if (files.contains(CrmHdl.SUBJECTS_TSV)) {
                processInputStream(CrmHdl.SUBJECTS_TSV, handler::importSubjects);
            }
            if (files.contains(CrmHdl.COMMENTS_TSV)) {
                processInputStream(CrmHdl.COMMENTS_TSV, handler::importComments);
            }
            close();
        }));
    }
}
