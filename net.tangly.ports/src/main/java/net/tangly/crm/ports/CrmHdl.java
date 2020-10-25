/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.crm.ports;

import java.nio.file.Path;
import javax.inject.Inject;

import net.tangly.bus.crm.RealmCrm;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the workflows defined for bounded domain activities in particular the import and export to files.
 */
public final class CrmHdl {
    public static final String MODULE = "net.tangly.ports";
    public static final String COMMENTS_TSV = "comments.tsv";
    public static final String LEGAL_ENTITIES_TSV = "legal-entities.tsv";
    public static final String NATURAL_ENTITIES_TSV = "natural-entities.tsv";
    public static final String EMPLOYEES_TSV = "employees.tsv";
    public static final String CONTRACTS_TSV = "contracts.tsv";
    public static final String INTERACTIONS_TSV = "interactions.tsv";
    public static final String ACTIVITIES_TSV = "activities.tsv";
    public static final String SUBJECTS_TSV = "subjects.tsv";
    public static final String VCARDS_FOLDER = "vcards";

    private static final Logger logger = LoggerFactory.getLogger(CrmHdl.class);
    private final RealmCrm realm;

    @Inject
    public CrmHdl(@NotNull RealmCrm realm) {
        this.realm = realm;
    }

    public RealmCrm realm() {
        return realm;
    }

    /**
     * Import all CRM domain entities defined in a set of TSV files.
     *
     * @param directory directory where the TSV files are stored
     * @see #exportEntities(Path)
     */
    public void importEntities(@NotNull Path directory) {
        CrmTsvHdl handler = new CrmTsvHdl(realm());
        handler.importLegalEntities(directory.resolve(LEGAL_ENTITIES_TSV));
        handler.importNaturalEntities(directory.resolve(NATURAL_ENTITIES_TSV));
        handler.importEmployees(directory.resolve(EMPLOYEES_TSV));
        handler.importContracts(directory.resolve(CONTRACTS_TSV));
        handler.importInteractions(directory.resolve(INTERACTIONS_TSV));
        handler.importActivities(directory.resolve(ACTIVITIES_TSV));
        handler.importSubjects(directory.resolve(SUBJECTS_TSV));
        handler.importComments(directory.resolve(COMMENTS_TSV));

        CrmVcardHdl crmVcardHdl = new CrmVcardHdl(realm());
        crmVcardHdl.importVCards(directory.resolve(VCARDS_FOLDER));
    }

    /**
     * Export all CRM domain entities into a set of TSV files.
     *
     * @param directory directory where the TSV files are stored
     * @see #importEntities(Path)
     */
    public void exportEntities(@NotNull Path directory) {
        CrmTsvHdl handler = new CrmTsvHdl(realm);
        handler.exportLegalEntities(directory.resolve(LEGAL_ENTITIES_TSV));
        handler.exportNaturalEntities(directory.resolve(NATURAL_ENTITIES_TSV));
        handler.exportEmployees(directory.resolve(EMPLOYEES_TSV));
        handler.exportContracts(directory.resolve(CONTRACTS_TSV));
        handler.exportInteractions(directory.resolve(INTERACTIONS_TSV));
        handler.exportActivities(directory.resolve(ACTIVITIES_TSV));
        handler.exportSubjects(directory.resolve(SUBJECTS_TSV));
        handler.exportComments(directory.resolve(COMMENTS_TSV));
    }
}
