/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.erp.crm.ports;

import net.tangly.core.domain.DomainAudit;
import net.tangly.erp.crm.services.CrmPort;
import net.tangly.erp.crm.services.CrmRealm;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Define the workflows defined for bounded domain activities in particular the import and export to files.
 */
public final class CrmAdapter implements CrmPort {
    public static final String COMMENTS_TSV = "comments.tsv";
    public static final String LEADS_TSV = "leads.tsv";
    public static final String LEGAL_ENTITIES_TSV = "legal-entities.tsv";
    public static final String NATURAL_ENTITIES_TSV = "natural-entities.tsv";
    public static final String EMPLOYEES_TSV = "employees.tsv";
    public static final String CONTRACTS_TSV = "contracts.tsv";
    public static final String CONTRACT_EXTENSIONS_TSV = "contract-extensions.tsv";
    public static final String OPPORTUNITIES_TSV = "opportunities.tsv";
    public static final String ACTIVITIES_TSV = "activities.tsv";
    public static final String VCARDS_FOLDER = "vcards";

    private final CrmRealm realm;
    private final Path folder;

    public CrmAdapter(@NotNull CrmRealm realm, @NotNull Path folder) {
        this.realm = realm;
        this.folder = folder;
    }

    @Override
    public CrmRealm realm() {
        return realm;
    }

    @Override
    public void importEntities(@NotNull DomainAudit audit) {
        var handler = new CrmTsvHdl(realm());
        handler.importLeads(audit, folder.resolve(LEADS_TSV));
        handler.importLegalEntities(audit, folder.resolve(LEGAL_ENTITIES_TSV));
        handler.importNaturalEntities(audit, folder.resolve(NATURAL_ENTITIES_TSV));
        handler.importEmployees(audit, folder.resolve(EMPLOYEES_TSV));
        handler.importContracts(audit, folder.resolve(CONTRACTS_TSV));
        handler.importContractExtensions(audit, folder.resolve(CONTRACT_EXTENSIONS_TSV));
        handler.importOpportunities(audit, folder.resolve(OPPORTUNITIES_TSV));
        handler.importActivities(audit, folder.resolve(ACTIVITIES_TSV));
        handler.importComments(audit, folder.resolve(COMMENTS_TSV));

        var crmEnrichmentHdl = new CrmEnrichmentHdl(realm());
        crmEnrichmentHdl.importVCards(folder.resolve(VCARDS_FOLDER));
        crmEnrichmentHdl.importLinkedInPhotos(folder);
    }

    @Override
    public void exportEntities(@NotNull DomainAudit audit) {
        var handler = new CrmTsvHdl(realm);
        handler.exportLeads(audit, folder.resolve(LEADS_TSV));
        handler.exportLegalEntities(audit, folder.resolve(LEGAL_ENTITIES_TSV));
        handler.exportNaturalEntities(audit, folder.resolve(NATURAL_ENTITIES_TSV));
        handler.exportEmployees(audit, folder.resolve(EMPLOYEES_TSV));
        handler.exportContracts(audit, folder.resolve(CONTRACTS_TSV));
        handler.exportContractExtensions(audit, folder.resolve(CONTRACT_EXTENSIONS_TSV));
        handler.exportOpportunities(audit, folder.resolve(OPPORTUNITIES_TSV));
        handler.exportActivities(audit, folder.resolve(ACTIVITIES_TSV));
        handler.exportComments(audit, folder.resolve(COMMENTS_TSV));
    }

    @Override
    public void clearEntities(@NotNull DomainAudit audit) {
        realm().activities().deleteAll();
        realm().opportunities().deleteAll();
        realm().contracts().deleteAll();
        realm().employees().deleteAll();
        realm().naturalEntities().deleteAll();
        realm().legalEntities().deleteAll();
        realm().leads().deleteAll();
    }
}
