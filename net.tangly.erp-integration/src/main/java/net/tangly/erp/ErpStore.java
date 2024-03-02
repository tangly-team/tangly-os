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

package net.tangly.erp;

import net.tangly.erp.crm.ports.CrmHdl;
import net.tangly.erp.invoices.ports.InvoicesHdl;
import net.tangly.erp.products.ports.ProductsHdl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Creates a CRM and ledger domain model store with all persistent values of all involved entities.
 * The entities are stored in TSV and JSON file as resources of the test component. The store copies all the resources in an in-memory file system for efficient tests.
 */
public record ErpStore(@NotNull FileSystem fs) {
    private static final String PACKAGE_NAME = "net/tangly/";
    private static final String CRM = "customers/";
    private static final String LEDGER = "ledger/";
    private static final String PRODUCTS = "products/";
    private static final String INVOICES = "invoices/";
    private static final String REPORTS = "reports/";
    private static final String VCARDS = "vcards/";
    private static final String PICTURES = "pictures/";
    private static final String CRM_PACKAGE_NAME = PACKAGE_NAME + CRM;
    private static final String LEDGER_PACKAGE_NAME = PACKAGE_NAME + LEDGER;
    private static final String PRODUCTS_PACKAGE_NAME = PACKAGE_NAME + PRODUCTS;
    private static final String INVOICES_PACKAGE_NAME = PACKAGE_NAME + INVOICES;
    private static final String VCARDS_PACKAGE_NAME = CRM_PACKAGE_NAME + VCARDS;
    private static final String PICTURES_PACKAGE_NAME = CRM_PACKAGE_NAME + PICTURES;
    private static final String REPORTS_PACKAGE_NAME = PACKAGE_NAME + REPORTS;
    private static final String ORGANIZATION = "/organization/";
    private static final String DATABASE = "db/";

    public Path organizationRoot() {
        return fs.getPath(ORGANIZATION);
    }

    public Path crmDb() {
        return fs.getPath(ORGANIZATION, "db/customers/");
    }

    public Path crmRoot() {
        return fs.getPath(ORGANIZATION, CRM);
    }

    public Path ledgerRoot() {
        return fs.getPath(ORGANIZATION, LEDGER);
    }

    public Path invoicesRoot() {
        return fs.getPath(ORGANIZATION, INVOICES);
    }

    public Path productsRoot() {
        return fs.getPath(ORGANIZATION, PRODUCTS);
    }

    public Path vcardsRoot() {
        return crmRoot().resolve(VCARDS);
    }

    public Path picturesRoot() {
        return crmRoot().resolve(PICTURES);
    }

    public Path reportsRoot() {
        return fs.getPath(ORGANIZATION, REPORTS);
    }

    public Path ledgerReportsRoot() {
        return reportsRoot().resolve(LEDGER);
    }

    public Path invoiceReportsRoot() {
        return reportsRoot().resolve(INVOICES);
    }

    /**
     * Sets up the test environment for integration tests of the CRM domain: CRM entities, invoices, and ledger.
     */
    public void createRepository() {
        try {
            Files.createDirectory(fs.getPath(ORGANIZATION));
            Files.createDirectory(fs.getPath(ORGANIZATION, CRM));
            Files.createDirectory(fs.getPath(ORGANIZATION, CRM, VCARDS));
            Files.createDirectory(fs.getPath(ORGANIZATION, CRM, PICTURES));
            Files.createDirectory(fs.getPath(ORGANIZATION, LEDGER));
            Files.createDirectory(fs.getPath(ORGANIZATION, PRODUCTS));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES, "2015/"));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES, "2016/"));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES, "2017/"));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES, "2018/"));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES, "2019/"));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES, "2020/"));
            Files.createDirectory(fs.getPath(ORGANIZATION, INVOICES, "2021/"));
            Files.createDirectory(fs.getPath(ORGANIZATION, REPORTS));
            Files.createDirectory(fs.getPath(ORGANIZATION, REPORTS, LEDGER));
            Files.createDirectory(fs.getPath(ORGANIZATION, REPORTS, INVOICES));
            Files.createDirectory(fs.getPath(ORGANIZATION, DATABASE));
            Files.createDirectory(crmDb());

            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.LEADS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.NATURAL_ENTITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.LEGAL_ENTITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.EMPLOYEES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.CONTRACTS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.INTERACTIONS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.ACTIVITIES_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.SUBJECTS_TSV);
            copy(CRM_PACKAGE_NAME, crmRoot(), CrmHdl.COMMENTS_TSV);
            copy(PRODUCTS_PACKAGE_NAME, productsRoot(), ProductsHdl.PRODUCTS_TSV);
            copy(PRODUCTS_PACKAGE_NAME, productsRoot(), ProductsHdl.ASSIGNMENTS_TSV);
            copy(PRODUCTS_PACKAGE_NAME, productsRoot(), ProductsHdl.EFFORTS_TSV);
            copy(LEDGER_PACKAGE_NAME, ledgerRoot(), "ledger.tsv");
            copy(LEDGER_PACKAGE_NAME, ledgerRoot(), "2015-journal.tsv");
            copy(LEDGER_PACKAGE_NAME, ledgerRoot(), "2016-journal.tsv");
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
            copy(PICTURES_PACKAGE_NAME, picturesRoot(), "marcelbaumann.jpeg");
            copy(REPORTS_PACKAGE_NAME, reportsRoot(), "trefoil.svg");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void copy(@NotNull String packageName, @NotNull Path folder, @NotNull String filename) throws IOException {
        Path resourcePath = Paths.get(Thread.currentThread().getContextClassLoader().getResource(packageName + filename).getPath());
        Files.copy(resourcePath, folder.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
    }
}
