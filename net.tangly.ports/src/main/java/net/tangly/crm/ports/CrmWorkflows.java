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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the workflows defined for CRM activities.
 */
public final class CrmWorkflows {
    public static final String LEGAL_ENTITIES_TSV = "legal-entities.tsv";
    public static final String NATURAL_ENTITIES_TSV = "natural-entities.tsv";
    public static final String EMPLOYEES_TSV = "employees.tsv";
    public static final String CONTRACTS_TSV = "contracts.tsv";
    private static final Logger logger = LoggerFactory.getLogger(CrmWorkflows.class);

    private final Crm crm;

    public CrmWorkflows(Crm crm) {
        this.crm = crm;
    }

    /**
     * Import all CRM domain entities defined in a set of TSV files.
     *
     * @param directory directory where the TSV files are stored
     * @throws IOException if an error occured when reading the files
     */
    public void importCrmEntitiesFromTsv(@NotNull Path directory) throws IOException {
        CrmTsvHdl handler = new CrmTsvHdl(crm);
        if (Files.exists(directory.resolve(LEGAL_ENTITIES_TSV))) {
            handler.importLegalEntities(directory.resolve(LEGAL_ENTITIES_TSV));
        } else {
            logger.atInfo().log("TSF File could not be found {}", LEGAL_ENTITIES_TSV);
        }
        if (Files.exists(directory.resolve(NATURAL_ENTITIES_TSV))) {
            handler.importNaturalEntities(directory.resolve(NATURAL_ENTITIES_TSV));
        } else {
            logger.atInfo().log("TSF File could not be found {}", NATURAL_ENTITIES_TSV);
        }
        if (Files.exists(directory.resolve(EMPLOYEES_TSV))) {
            handler.importEmployees(directory.resolve(EMPLOYEES_TSV));
        } else {
            logger.atInfo().log("TSF File could not be found {}", EMPLOYEES_TSV);
        }
        if (Files.exists(directory.resolve(CONTRACTS_TSV))) {
            handler.importContracts(directory.resolve(CONTRACTS_TSV));
        } else {
            logger.atInfo().log("TSF File could not be found {}", CONTRACTS_TSV);
        }
    }
}
