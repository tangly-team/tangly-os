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

package net.tangly.erp;

import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.products.ports.ProductsAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;

/**
 * Creates a CRM and ledger domain model store with all persistent values of all involved entities.
 * The entities are stored in TSV and JSON file as resources of the test component. The store copies all the resources in an in-memory file system for efficient tests.
 */
public record ErpStore(@NotNull FileSystem fs) {
    static final String ORGANIZATION = "/organization/";
    static final String DATABASE = "db/";
    static final String DATA = "data/";
    static final String REPORTS = "reports/";

    static final String CRM = "customers/";
    static final String LEDGER = "transactions/";
    static final String PRODUCTS = "products/";
    static final String INVOICES = "invoices/";

    static final String VCARDS = "vcards/";
    static final String PICTURES = "pictures/";

    static final String PACKAGE_NAME = "net/tangly/";
    static final String CRM_PACKAGE_NAME = PACKAGE_NAME + CRM;
    static final String LEDGER_PACKAGE_NAME = PACKAGE_NAME + LEDGER;
    static final String PRODUCTS_PACKAGE_NAME = PACKAGE_NAME + PRODUCTS;
    static final String INVOICES_PACKAGE_NAME = PACKAGE_NAME + INVOICES;

    static final String VCARDS_PACKAGE_NAME = CRM_PACKAGE_NAME + VCARDS;
    static final String PICTURES_PACKAGE_NAME = CRM_PACKAGE_NAME + PICTURES;
    static final String REPORTS_PACKAGE_NAME = PACKAGE_NAME + REPORTS;


    public Path dbRoot() {
        return fs.getPath(ORGANIZATION, "db/");
    }

    public Path dataRoot() {
        return fs.getPath(ORGANIZATION, "data");
    }

    public Path reportsRoot() {
        return fs.getPath(ORGANIZATION, "reports");
    }


    /**
     * Sets up the test environment for integration tests of the CRM domain: CRM entities, invoices, and ledger.
     */
    public void createRepository() {
        try {
            Files.createDirectory(fs.getPath(ORGANIZATION));

            createDomainPaths(CRM);
            Files.createDirectory(dataRoot().resolve(CRM, VCARDS));
            Files.createDirectory(dataRoot().resolve(CRM, PICTURES));

            createDomainPaths(LEDGER);

            createDomainPaths(PRODUCTS);
            createYearFolders(dataRoot().resolve(PRODUCTS));
            createYearFolders(reportsRoot().resolve(PRODUCTS));

            createDomainPaths(INVOICES);
            createYearFolders(dataRoot().resolve(INVOICES));
            createYearFolders(reportsRoot().resolve(INVOICES));

            var crmRoot = dataRoot().resolve(CRM);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.LEADS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.NATURAL_ENTITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.LEGAL_ENTITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.EMPLOYEES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.CONTRACTS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.CONTRACT_EXTENSIONS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.INTERACTIONS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.ACTIVITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.SUBJECTS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot, CrmAdapter.COMMENTS_TSV);
            copy(VCARDS_PACKAGE_NAME, crmRoot.resolve(VCARDS), "1-MarcelBaumann.vcf");
            copy(PICTURES_PACKAGE_NAME, crmRoot.resolve(PICTURES), "marcelbaumann.jpeg");

            var productsRoot = dataRoot().resolve(PRODUCTS);
            copy(PRODUCTS_PACKAGE_NAME, productsRoot, ProductsAdapter.PRODUCTS_TSV);
            copy(PRODUCTS_PACKAGE_NAME, productsRoot, ProductsAdapter.WORK_CONTRACTS_TSV);
            copy(PRODUCTS_PACKAGE_NAME, productsRoot, ProductsAdapter.ASSIGNMENTS_TSV);
            copy(PRODUCTS_PACKAGE_NAME, productsRoot, ProductsAdapter.EFFORTS_TSV);
            copy(STR."\{PRODUCTS_PACKAGE_NAME}2020/", productsRoot.resolve("2020"), "2020-efforts.yaml");

            var ledgerRoot = dataRoot().resolve(LEDGER);
            copy(LEDGER_PACKAGE_NAME, ledgerRoot, "ledger.tsv");
            copy(LEDGER_PACKAGE_NAME, ledgerRoot, "2015-journal.tsv");
            copy(LEDGER_PACKAGE_NAME, ledgerRoot, "2016-journal.tsv");

            var invoicesRoot = dataRoot().resolve(INVOICES);
            copy(INVOICES_PACKAGE_NAME, invoicesRoot, InvoicesAdapter.ARTICLES_TSV);
            copy(STR."\{INVOICES_PACKAGE_NAME}2015/", invoicesRoot.resolve("2015"), "2015-8001-Invoice-HSLU-December.json");
            copy(STR."\{INVOICES_PACKAGE_NAME}2016/", invoicesRoot.resolve("2016"), "2016-8001-Invoice-HSLU-October.json");
            copy(STR."\{INVOICES_PACKAGE_NAME}2017/", invoicesRoot.resolve("2017"), "2017-8001-Invoice-HSLU-February.json");
            copy(STR."\{INVOICES_PACKAGE_NAME}2017/", invoicesRoot.resolve("2017"), "2017-8022-Invoice-HSLU-December.json");
            copy(STR."\{INVOICES_PACKAGE_NAME}2018/", invoicesRoot.resolve("2018"), "2018-8001-Invoice-HSLU-January.json");
            copy(STR."\{INVOICES_PACKAGE_NAME}2018/", invoicesRoot.resolve("2018"), "2018-8022-Invoice-HSLU-November.json");
            copy(STR."\{INVOICES_PACKAGE_NAME}2019/", invoicesRoot.resolve("2019"), "2019-8020-Invoice-HSLU-December.json");
            copy(STR."\{INVOICES_PACKAGE_NAME}2020/", invoicesRoot.resolve("2020"), "2020-8001-Invoice-HSLU-May.json");

            copy(REPORTS_PACKAGE_NAME, reportsRoot(), "trefoil.svg");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createDomainPaths(String domain) throws IOException {
        Files.createDirectories(fs.getPath(ORGANIZATION, DATA, domain));
        Files.createDirectories(fs.getPath(ORGANIZATION, DATABASE, domain));
        Files.createDirectories(fs.getPath(ORGANIZATION, REPORTS, domain));
    }

    private void createYearFolders(Path folder) throws IOException {
        for (int year = 2015; year <= 2024; year++) {
            Files.createDirectory(folder.resolve(STR."\{year}"));
        }
    }

    private static void copy(@NotNull String packageName, @NotNull Path folder, @NotNull String filename) throws IOException {
        Path resourcePath = Paths.get(Thread.currentThread().getContextClassLoader().getResource(packageName + filename).getPath());
        Files.copy(resourcePath, folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }
}
