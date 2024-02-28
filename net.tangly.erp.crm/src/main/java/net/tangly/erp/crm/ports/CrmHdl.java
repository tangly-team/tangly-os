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

package net.tangly.erp.crm.ports;

import net.tangly.core.domain.Handler;
import net.tangly.erp.crm.services.CrmHandler;
import net.tangly.erp.crm.services.CrmRealm;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Define the workflows defined for bounded domain activities in particular the import and export to files.
 */
public final class CrmHdl implements CrmHandler {
    public static final String COMMENTS_TSV = "comments.tsv";
    public static final String LEADS_TSV = "leads.tsv";
    public static final String LEGAL_ENTITIES_TSV = "legal-entities.tsv";
    public static final String NATURAL_ENTITIES_TSV = "natural-entities.tsv";
    public static final String EMPLOYEES_TSV = "employees.tsv";
    public static final String CONTRACTS_TSV = "contracts.tsv";
    public static final String INTERACTIONS_TSV = "interactions.tsv";
    public static final String ACTIVITIES_TSV = "activities.tsv";
    public static final String SUBJECTS_TSV = "subjects.tsv";
    public static final String VCARDS_FOLDER = "vcards";

    private final CrmRealm realm;
    private final Path folder;

    public CrmHdl(@NotNull CrmRealm realm, @NotNull Path folder) {
        this.realm = realm;
        this.folder = folder;
    }

    @Override
    public CrmRealm realm() {
        return realm;
    }

    @Override
    public void importEntities() {
        var handler = new CrmTsvHdl(realm());
        Handler.importEntities(folder, LEADS_TSV, handler::importLeads);
        Handler.importEntities(folder, LEGAL_ENTITIES_TSV, handler::importLegalEntities);
        Handler.importEntities(folder, NATURAL_ENTITIES_TSV, handler::importNaturalEntities);
        Handler.importEntities(folder, EMPLOYEES_TSV, handler::importEmployees);
        Handler.importEntities(folder, CONTRACTS_TSV, handler::importContracts);
        Handler.importEntities(folder, INTERACTIONS_TSV, handler::importInteractions);
        Handler.importEntities(folder, ACTIVITIES_TSV, handler::importActivities);
        Handler.importEntities(folder, SUBJECTS_TSV, handler::importSubjects);
        Handler.importEntities(folder, COMMENTS_TSV, handler::importComments);
        CrmVcardHdl crmVcardHdl = new CrmVcardHdl(realm());
        crmVcardHdl.importVCards(folder.resolve(VCARDS_FOLDER));
    }

    @Override
    public void exportEntities() {
        var handler = new CrmTsvHdl(realm);
        handler.exportLeads(folder.resolve(LEADS_TSV));
        handler.exportLegalEntities(folder.resolve(LEGAL_ENTITIES_TSV));
        handler.exportNaturalEntities(folder.resolve(NATURAL_ENTITIES_TSV));
        handler.exportEmployees(folder.resolve(EMPLOYEES_TSV));
        handler.exportContracts(folder.resolve(CONTRACTS_TSV));
        handler.exportInteractions(folder.resolve(INTERACTIONS_TSV));
        handler.exportActivities(folder.resolve(ACTIVITIES_TSV));
        handler.exportSubjects(folder.resolve(SUBJECTS_TSV));
        handler.exportComments(folder.resolve(COMMENTS_TSV));
    }

    @Override
    public void clearEntities() {
        realm().subjects().deleteAll();
        realm().activities().deleteAll();
        realm().interactions().deleteAll();
        realm().contracts().deleteAll();
        realm().employees().deleteAll();
        realm().naturalEntities().deleteAll();
        realm().legalEntities().deleteAll();
        realm().leads().deleteAll();
    }
}
