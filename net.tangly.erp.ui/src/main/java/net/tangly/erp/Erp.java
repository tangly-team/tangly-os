/*
 * Copyright 2016-2024 Marcel Baumann
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

import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.core.Entity;
import net.tangly.core.HasOid;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmEntities;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The ERP application instantiating the bounded domain instances. The class implements a modular monolith application.
 */
public class Erp {
    private static final String DATABASES_DIRECTORY_PROPERTY = "erp.root.db.directory";
    private static final String IMPORTS_DIRECTORY_PROPERTY = "erp.root.imports.directory";
    private static final String REPORTS_DIRECTORY_PROPERTY = "erp.root.reports.directory";

    private static final Logger logger = LogManager.getLogger();
    private static Erp self;
    private static Lock lock = new ReentrantLock();
    private final TypeRegistry registry;
    private final Properties properties;
    private CrmBoundedDomain crmBoundedDomain;
    private ProductsBoundedDomain productsBoundedDomain;
    private InvoicesBoundedDomain invoicesBoundedDomain;
    private LedgerBoundedDomain ledgerBoundedDomain;

    /**
     * Defines a private constructor to provide singleton approach.
     */
    private Erp() {
        this.registry = new TypeRegistry();
        this.properties = new Properties();
        load();
        assert (self == null);
        self = this;
    }

    public static Erp instance() {
        return self;
    }

    public static Erp propertiesConfiguredErp() {
        lock.lock();
        try {
            if (self == null) {
                self = new Erp();
                self.createDomains();
            }
        } finally {
            lock.unlock();
        }
        return self;
    }

    public static Erp inMemoryErp() {
        lock.lock();
        try {
            if (self == null) {
                self = new Erp();
                self.properties.remove(DATABASES_DIRECTORY_PROPERTY);
                self.createDomains();
            }
        } finally {
            lock.unlock();
        }
        return self;
    }

    public CrmBoundedDomain crmBoundedDomain() {
        return crmBoundedDomain;
    }

    public ProductsBoundedDomain productsBoundedDomain() {
        return productsBoundedDomain;
    }

    public InvoicesBoundedDomain invoicesBoundedDomain() {
        return invoicesBoundedDomain;
    }

    public LedgerBoundedDomain ledgerBoundedDomain() {
        return ledgerBoundedDomain;
    }

    private void createDomains() {
        crmBoundedDomain = ofCrmDomain();
        productsBoundedDomain = ofProductsDomain();
        invoicesBoundedDomain = ofInvoicesDomain();
        ledgerBoundedDomain = ofLedgerDomain();
    }

    private static Subject createAdminSubject() {
        var subject = new Subject(Entity.UNDEFINED_OID);
        ReflectionUtilities.set(subject, HasOid.OID, 900);
        subject.id("aeon");
        subject.newPassword("aeon");
        subject.from(LocalDate.of(2000, Month.JANUARY, 1));
        return subject;
    }

    private InvoicesBoundedDomain ofInvoicesDomain() {
        if (isEnabled(InvoicesBoundedDomain.DOMAIN)) {
            return null;
        } else {
            var realm = (databases() == null) ? new InvoicesEntities() : new InvoicesEntities(Path.of(databases(), InvoicesBoundedDomain.DOMAIN));
            return new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm), new InvoicesAdapter(realm, Path.of(reports())), registry);
        }
    }

    private CrmBoundedDomain ofCrmDomain() {
        if (isEnabled(CrmBoundedDomain.DOMAIN)) {
            return null;
        } else {
            var realm = (databases() == null) ? new CrmEntities() : new CrmEntities(Path.of(databases(), CrmBoundedDomain.DOMAIN));
            if (realm.subjects().items().isEmpty()) {
                realm.subjects().update(createAdminSubject());
            }
            return new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmAdapter(realm, Path.of(imports())), registry);
        }
    }

    private ProductsBoundedDomain ofProductsDomain() {
        if (isEnabled(ProductsBoundedDomain.DOMAIN)) {
            return null;
        } else {
            var realm = (databases() == null) ? new ProductsEntities() : new ProductsEntities(Path.of(databases(), ProductsBoundedDomain.DOMAIN));
            var logic = new ProductsBusinessLogic(realm);
            return new ProductsBoundedDomain(realm, logic, new ProductsAdapter(realm, logic, Path.of(imports(), ProductsBoundedDomain.DOMAIN)), registry);
        }
    }

    private void load() {
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/application.properties"));
        } catch (IOException e) {
            logger.atError().log("Application Configuration Load Error {}", e);
        }
    }

    private boolean isPersistenceStoreAvailable() {
        String rootPersistenceFolder = properties.getProperty("erp.root.directory");
        return (rootPersistenceFolder != null) && Files.exists(Paths.get(rootPersistenceFolder));
    }

    private LedgerBoundedDomain ofLedgerDomain() {
        if (isEnabled(LedgerBoundedDomain.DOMAIN)) {
            return null;
        } else {
            var realm = (databases() == null) ? new LedgerEntities() : new LedgerEntities(Path.of(databases(), LedgerBoundedDomain.DOMAIN));
            return new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm), new LedgerAdapter(realm, Path.of(reports())), registry);
        }
    }

    private boolean isEnabled(@NotNull String domain) {
        return Boolean.parseBoolean(properties.getProperty(STR."\{domain}.enabled", "false"));
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
}
