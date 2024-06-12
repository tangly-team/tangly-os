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
import net.tangly.app.services.AppsBoundedDomain;
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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    private static final Logger logger = LogManager.getLogger();
    private static int port = 8080;

    public static void main(@NotNull String[] args) throws Exception {
        final String contextRoot = "/erp";
        parse(args);
        ofDomains();
        ofDomainRests();
        Application.instance().startup();
        new VaadinBoot() {
            @Override
            protected @NotNull WebAppContext createWebAppContext() throws IOException {
                final WebAppContext context = super.createWebAppContext();
                ServletHolder staticFiles = new ServletHolder("staticFiles", new DefaultServlet());
                staticFiles.setInitParameter("resourceBase", "/private/var/tangly-erp/reports");
                context.addServlet(staticFiles, "/reports/*");
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
            .desc("path to the application configuration file").build());
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
        } catch (NumberFormatException | ParseException e) {
            logger.atError().log("Parsing failed.  Reason: {}", e.getMessage());
        }
    }

    public static void ofDomains() {
        Application application = Application.instance();
        if (application.apps().realm().users().items().isEmpty()) {
            application.apps().realm().users().update(createDefaultUser());
        }
        if (application.isEnabled(CrmBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new CrmEntities() : new CrmEntities(Path.of(application.databases(), CrmBoundedDomain.DOMAIN));
            var domain = new CrmBoundedDomain(realm, new CrmBusinessLogic(realm),
                new CrmAdapter(realm, Path.of(Application.instance().imports(CrmBoundedDomain.DOMAIN))), application.registry());
            application.registerBoundedDomain(domain);
        }
        if (application.isEnabled(InvoicesBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new InvoicesEntities() : new InvoicesEntities(Path.of(application.databases(), InvoicesBoundedDomain.DOMAIN));
            var domain = new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm),
                new InvoicesAdapter(realm, Path.of(application.imports(InvoicesBoundedDomain.DOMAIN)),
                    Path.of(application.reports(InvoicesBoundedDomain.DOMAIN))), application.registry());
            application.registerBoundedDomain(domain);
        }
        if (application.isEnabled(LedgerBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new LedgerEntities() : new LedgerEntities(Path.of(application.databases(), LedgerBoundedDomain.DOMAIN));
            var domain = new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm),
                new LedgerAdapter(realm, Path.of(Application.instance().imports(LedgerBoundedDomain.DOMAIN)),
                    Path.of(application.reports(LedgerBoundedDomain.DOMAIN))), application.registry());
            application.registerBoundedDomain(domain);
        }
        if (application.isEnabled(ProductsBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new ProductsEntities() : new ProductsEntities(Path.of(application.databases(), ProductsBoundedDomain.DOMAIN));
            var logic = new ProductsBusinessLogic(realm);
            var domain = new ProductsBoundedDomain(realm, logic,
                new ProductsAdapter(realm, logic, Path.of(Application.instance().imports(ProductsBoundedDomain.DOMAIN)),
                    Path.of(application.reports(ProductsBoundedDomain.DOMAIN))), application.registry());
            application.registerBoundedDomain(domain);
        }
        if (application.isEnabled(CollaboratorsBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new CollaboratorsEntities() :
                new CollaboratorsEntities(Path.of(application.databases(), CollaboratorsBoundedDomain.DOMAIN));
            var domain = new CollaboratorsBoundedDomain(realm, new CollaboratorsBusinessLogic(realm),
                new CollaboratorsAdapter(realm, Path.of(Application.instance().imports(CollaboratorsBoundedDomain.DOMAIN))), application.registry());
            application.registerBoundedDomain(domain);
        }
    }

    public static void ofDomainRests() {
        Application application = Application.instance();
        if (application.isEnabled(CrmBoundedDomain.DOMAIN)) {
            var rest = new CrmBoundedDomainRest((CrmBoundedDomain) application.getBoundedDomain(CrmBoundedDomain.DOMAIN).orElseThrow());
            application.registerBoundedDomainRest(rest);
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
