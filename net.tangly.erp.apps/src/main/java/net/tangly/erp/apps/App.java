/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.apps;

import net.tangly.core.TypeRegistry;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.ports.InvoicesHdl;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Properties;

public class App {
    private static final String DATABASES_DIRECTORY_PROPERTY = "erp.root.db.directory";
    private static final String IMPORTS_DIRECTORY_PROPERTY = "erp.root.imports.directory";
    private static final String REPORTS_DIRECTORY_PROPERTY = "erp.root.reports.directory";
    private static final String PROPERTIES_FILE = "application.properties";
    private static final Logger logger = LogManager.getLogger();
    private final TypeRegistry registry;
    private final Properties properties;

    public App() {
        this.registry = new TypeRegistry();
        this.properties = new Properties();
        load(PROPERTIES_FILE);
    }

    public static void main(String[] arguments) {
        logger.info("Application started");
        var app = new App();
        InvoicesBoundedDomain invoices = app.ofInvoicesDomain();
        invoices.handler().importEntities();
        invoices.port().exportInvoiceDocuments(false, false, null, null);
        logger.info("Application ended");
    }

    public InvoicesBoundedDomain ofInvoicesDomain() {
        var realm = new InvoicesEntities();
        return new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm), new InvoicesHdl(realm, Path.of(imports(), InvoicesBoundedDomain.DOMAIN)),
            new InvoicesAdapter(realm, Path.of(reports(), InvoicesBoundedDomain.DOMAIN)), registry);
    }

    private void load(String propertiesFile) {
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile));
        } catch (IOException e) {
            logger.atError().log("Application Configuration Load Error", e);
            throw new UncheckedIOException(e);
        }
    }

    private String databases() {
        return properties.getProperty(DATABASES_DIRECTORY_PROPERTY);
    }

    private String imports() {
        return properties.getProperty(IMPORTS_DIRECTORY_PROPERTY);
    }

    private String reports() {
        return properties.getProperty(REPORTS_DIRECTORY_PROPERTY);
    }

    private void create() {
        Options options = new Options();
        options.addOption("create-invoices", false, "create invoices artifacts");
        options.addOption("i", true, "invoice identifier of the artifact to create");
        options.addOption("from", true, "beginning of the time interval to filter invoices");
        options.addOption("to", true, "end of the time interval to filter invoices");
        options.addOption("qrswiss", false, "flag indicated the invoice artifact should contain a Swiss QR code");
        options.addOption("facturX", false, "flag indicated the invoice artifact should contain a facture X load");
    }
}
