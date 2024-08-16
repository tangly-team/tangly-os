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

package net.tangly.erp.ui;

import com.github.mvysny.vaadinboot.VaadinBoot;
import net.tangly.app.Application;
import net.tangly.app.Tenant;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.commons.logger.EventData;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.core.domain.User;
import net.tangly.erp.collaborators.ports.CollaboratorsAdapter;
import net.tangly.erp.collaborators.ports.CollaboratorsEntities;
import net.tangly.erp.collabortors.services.CollaboratorsBoundedDomain;
import net.tangly.erp.collabortors.services.CollaboratorsBusinessLogic;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmEntities;
import net.tangly.erp.crm.rest.CrmBoundedDomainRest;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import net.tangly.erp.products.ports.ProductsAdapter;
import net.tangly.erp.products.ports.ProductsEntities;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.io.RuntimeIOException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * Entry point to the start of the regular Java SE application with an embedded Jetty server. The application parameters are:
 * <dl>
 *     <dt>port</dt>
 *     <dd>The parameter defines the listening port of the Jetty embedded server.</dd>
 *     <dt>mode</dt>
 *     <dd>The parameter defines the mode of the application.
 *     The application is either using an in-memory data or has a persistent storage where import data can be provided and updated stored in the database.</dd>
 * </dl>
 */
public final class Main {
    public static final String TENANCY_CONFIGURATION_FOLDER = "/var/tangly-erp/_tenants";
    private static final Logger logger = LogManager.getLogger();
    private static int port = 8080;
    private static String propertyFile;

    public static void main(@NotNull String[] args) throws Exception {
        final String contextRoot = "/erp";
        EventData.of("application started with arguments", "Application", EventData.Status.INFO, args.toString(), null, null);
        parse(args);
        if (propertyFile == null) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(TENANCY_CONFIGURATION_FOLDER))) {
                for (Path path : stream) {
                    if (!Files.isDirectory(path)) {
                        Application.instance().putTenant(createTenant(path));
                    }
                }
            }
        } else {
            Application.instance().putTenant(createTenant(Paths.get(propertyFile)));
        }
        new VaadinBoot() {
            @Override
            protected @NotNull WebAppContext createWebAppContext() throws IOException {
                var tenant = Application.instance().tenant("tangly");
                final WebAppContext context = super.createWebAppContext();
                ServletHolder staticFiles = new ServletHolder("staticFiles", new DefaultServlet());
                String docsFolder = tenant.getProperty("tenant.root.docs.directory");
                staticFiles.setInitParameter("resourceBase", docsFolder);
                String tenantName = tenant.getProperty("tenant.name");
                context.addServlet(staticFiles, "/" + tenantName + "/docs/*");
                return context;
            }
        }.setPort(port).withContextRoot(contextRoot).run();
    }

    private static Options options() {
        var options = new Options();
        options.addOption(Option.builder("h").longOpt("help").hasArg(false).desc("print this help message").build());
        options.addOption(
            Option.builder("p").longOpt("port").type(Integer.TYPE).argName("port").hasArg().desc("listening port of the embedded server").build());
        options.addOption(Option.builder("c").longOpt("configuration").type(String.class).argName("configuration-file").hasArg()
            .desc("uri to the default tenant properties configuration file").build());
        return options;
    }

    private static void parse(@NotNull String[] args) {
        var parser = new DefaultParser();
        var options = options();
        try {
            var line = parser.parse(options, args);
            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("tangly ERP", options);
            }
            port = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : 8080;
            propertyFile = (line.hasOption("c")) ? line.getOptionValue("c").trim() : null;
        } catch (NumberFormatException | ParseException e) {
            logger.atError().log("Parsing failed.  Reason: {}", e.getMessage());
        }
    }

    public static Tenant createTenant(@NotNull Path propertiesPath) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(propertiesPath)) {
            properties.load(stream);
        } catch (IOException e) {
            logger.atError().log("Tenant configuration properties load error {}", e);
            throw new RuntimeIOException(e);
        }
        Tenant tenant = new Tenant(properties);
        ofDomains(tenant);
        ofDomainRests(tenant);
        return tenant;
    }

    public static void ofDomains(@NotNull Tenant tenant) {
        if (tenant.apps().realm().users().items().isEmpty()) {
            tenant.apps().realm().users().update(createDefaultUser());
        }
        if (tenant.isEnabled(CrmBoundedDomain.DOMAIN)) {
            var realm = tenant.inMemory() ? new CrmEntities() : new CrmEntities(Path.of(tenant.databases(), CrmBoundedDomain.DOMAIN));
            var domain =
                new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmAdapter(realm, Path.of(tenant.imports(CrmBoundedDomain.DOMAIN))), tenant);
            tenant.registerBoundedDomain(domain);
        }
        if (tenant.isEnabled(InvoicesBoundedDomain.DOMAIN)) {
            var realm = tenant.inMemory() ? new InvoicesEntities() : new InvoicesEntities(Path.of(tenant.databases(), InvoicesBoundedDomain.DOMAIN));
            var domain = new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm),
                new InvoicesAdapter(realm, Path.of(tenant.imports(InvoicesBoundedDomain.DOMAIN)), Path.of(tenant.docs(InvoicesBoundedDomain.DOMAIN))), tenant);
            tenant.registerBoundedDomain(domain);
        }
        if (tenant.isEnabled(LedgerBoundedDomain.DOMAIN)) {
            var realm = tenant.inMemory() ? new LedgerEntities() : new LedgerEntities(Path.of(tenant.databases(), LedgerBoundedDomain.DOMAIN));
            var domain = new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm),
                new LedgerAdapter(realm, tenant.registry(), Path.of(tenant.imports(LedgerBoundedDomain.DOMAIN)),
                    Path.of(tenant.docs(LedgerBoundedDomain.DOMAIN))), tenant);
            tenant.registerBoundedDomain(domain);
        }
        if (tenant.isEnabled(ProductsBoundedDomain.DOMAIN)) {
            var realm = tenant.inMemory() ? new ProductsEntities() : new ProductsEntities(Path.of(tenant.databases(), ProductsBoundedDomain.DOMAIN));
            var logic = new ProductsBusinessLogic(realm);
            var domain = new ProductsBoundedDomain(realm, logic,
                new ProductsAdapter(realm, logic, Path.of(tenant.imports(ProductsBoundedDomain.DOMAIN)), Path.of(tenant.docs(ProductsBoundedDomain.DOMAIN))),
                tenant);
            tenant.registerBoundedDomain(domain);
        }
        if (tenant.isEnabled(CollaboratorsBoundedDomain.DOMAIN)) {
            var realm =
                tenant.inMemory() ? new CollaboratorsEntities() : new CollaboratorsEntities(Path.of(tenant.databases(), CollaboratorsBoundedDomain.DOMAIN));
            var domain = new CollaboratorsBoundedDomain(realm, new CollaboratorsBusinessLogic(realm),
                new CollaboratorsAdapter(realm, Path.of(tenant.imports(CollaboratorsBoundedDomain.DOMAIN))), tenant);
            tenant.registerBoundedDomain(domain);
        }
    }

    public static void ofDomainRests(@NotNull Tenant tenant) {
        Application application = Application.instance();
        if (tenant.isEnabled(CrmBoundedDomain.DOMAIN)) {
            var rest = new CrmBoundedDomainRest((CrmBoundedDomain) tenant.getBoundedDomain(CrmBoundedDomain.DOMAIN).orElseThrow());
            tenant.registerBoundedDomainRest(rest);
        }
    }

    private static User createDefaultUser() {
        final String username = "aeon";
        String passwordSalt = User.newSalt();
        String passwordHash = User.encryptPassword("aeon", passwordSalt);
        var rights = List.of(new AccessRights(username, AppsBoundedDomain.DOMAIN, AccessRightsCode.domainAdmin));
        return new User(username, passwordHash, passwordSalt, true, null, rights, null);
    }
}
