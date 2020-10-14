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
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import net.tangly.invoices.ports.InvoicesHdl;
import org.jetbrains.annotations.NotNull;

/**
 * Create a CRM and ledger domain model store with all persistent values of all involved entities. The entities are stored in TSV and JSON file as resources of
 * the test component. The store copies all the resources in an in-memory file system for efficient tests.
 */
class CrmAndLedgerStore {
    private static final String PACKAGE_NAME = "net/tangly/";
    private static final String CRM_PACKAGE_NAME = PACKAGE_NAME + "crm/";
    private static final String LEDGER_PACKAGE_NAME = PACKAGE_NAME + "ledger/";
    private static final String INVOICES_PACKAGE_NAME = PACKAGE_NAME + "invoices/";
    private static final String VCARDS_PACKAGE_NAME = CRM_PACKAGE_NAME + "vcards/";

    private final FileSystem fs;

    public CrmAndLedgerStore(@NotNull FileSystem fs) {
        this.fs = fs;
    }

    public Path organizationRoot() {
        return fs.getPath("/organization/");
    }

    public Path crmRoot() {
        return fs.getPath("/organization/crm/");
    }

    public Path ledgerRoot() {
        return fs.getPath("/organization/ledger/");
    }

    public Path invoicesRoot() {
        return fs.getPath("/organization/invoices/");
    }

    public Path vcardsRoot() {
        return fs.getPath("/organization/crm/vcards/");
    }

    public Path ledgerReportsRoot() {
        return fs.getPath("/organization/reports/ledger/");
    }

    /**
     * Set up the test environment for integration tests of the CRM domain: CRM entities, invoices, and ledger.
     */
    public void createCrmAndLedgerRepository() {
        try {
            Files.createDirectory(fs.getPath("/organization/"));
            Files.createDirectory(fs.getPath("/organization/crm/"));
            Files.createDirectory(fs.getPath("/organization/crm/vcards/"));
            Files.createDirectory(fs.getPath("/organization/ledger/"));
            Files.createDirectory(fs.getPath("/organization/invoices/"));
            Files.createDirectory(fs.getPath("/organization/invoices/2015/"));
            Files.createDirectory(fs.getPath("/organization/invoices/2016/"));
            Files.createDirectory(fs.getPath("/organization/invoices/2017/"));
            Files.createDirectory(fs.getPath("/organization/invoices/2018/"));
            Files.createDirectory(fs.getPath("/organization/invoices/2019/"));
            Files.createDirectory(fs.getPath("/organization/invoices/2020/"));
            Files.createDirectory(fs.getPath("/organization/reports/"));
            Files.createDirectory(fs.getPath("/organization/reports/ledger/"));
            Files.createDirectory(fs.getPath("/organization/reports/invoices/"));

            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.NATURAL_ENTITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.LEGAL_ENTITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.EMPLOYEES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.CONTRACTS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.INTERACTIONS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.ACTIVITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.SUBJECTS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.COMMENTS_TSV);

            copy(LEDGER_PACKAGE_NAME, ledgerRoot(), "swiss-ledger.tsv");
            copy(LEDGER_PACKAGE_NAME, ledgerRoot(), "transactions-2015-2016.tsv");

            copy(INVOICES_PACKAGE_NAME, invoicesRoot(), InvoicesHdl.ARTICLES_TSV);
            copy(INVOICES_PACKAGE_NAME + "2015/", invoicesRoot().resolve("2015"), "2015-8001-Invoice-HSLU-December.json");
            copy(INVOICES_PACKAGE_NAME + "2016/", invoicesRoot().resolve("2016"), "2016-8001-Invoice-HSLU-October.json");
            copy(INVOICES_PACKAGE_NAME + "2017/", invoicesRoot().resolve("2017"), "2017-8001-Invoice-HSLU-February.json");
            copy(INVOICES_PACKAGE_NAME + "2017/", invoicesRoot().resolve("2017"), "2017-8022-Invoice-HSLU-December.json");
            copy(INVOICES_PACKAGE_NAME + "2018/", invoicesRoot().resolve("2018"), "2018-8001-Invoice-HSLU-January.json");
            copy(INVOICES_PACKAGE_NAME + "2018/", invoicesRoot().resolve("2018"), "2018-8022-Invoice-HSLU-November.json");
            copy(INVOICES_PACKAGE_NAME + "2019/", invoicesRoot().resolve("2019"), "2019-8020-Invoice-HSLU-December.json");
            copy(INVOICES_PACKAGE_NAME + "2020/", invoicesRoot().resolve("2020"), "2020-8001-Invoice-HSLU-May.json");

            copy(VCARDS_PACKAGE_NAME, vcardsRoot(), "1-MarcelBaumann.vcf");

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void copy(@NotNull String packageName, @NotNull Path folder, @NotNull String filename) throws IOException {
        Path resourcePath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(packageName + filename).getPath()));
        Files.copy(resourcePath, folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }
}
