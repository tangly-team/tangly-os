/*
 * Copyright 2016-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp;

import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.core.HasOid;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.ports.InvoicesHdl;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.ports.LedgerHdl;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import net.tangly.erp.products.ports.ProductsAdapter;
import net.tangly.erp.products.ports.ProductsEntities;
import net.tangly.erp.products.ports.ProductsHdl;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erpr.crm.ports.CrmEntities;
import net.tangly.erpr.crm.ports.CrmHdl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
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
            }
        } finally {
            lock.unlock();
        }
        return self;
    }

    public InvoicesBoundedDomain ofInvoicesDomain() {
        var realm = (databases() == null) ? new InvoicesEntities() : new InvoicesEntities(Path.of(databases(), InvoicesBoundedDomain.DOMAIN));
        return new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm), new InvoicesHdl(realm, Path.of(imports(), InvoicesBoundedDomain.DOMAIN)),
            new InvoicesAdapter(realm, Path.of(reports(), InvoicesBoundedDomain.DOMAIN)), registry);
    }

    public CrmBoundedDomain ofCrmDomain() {
        var realm = (databases() == null) ? new CrmEntities() : new CrmEntities(Path.of(databases(), CrmBoundedDomain.DOMAIN));
        if (realm.subjects().items().isEmpty()) {
            realm.subjects().update(createAdminSubject());
        }
        return new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmHdl(realm, Path.of(imports(), CrmBoundedDomain.DOMAIN)), null, registry);
    }

    public ProductsBoundedDomain ofProductsDomain() {
        var realm = (databases() == null) ? new ProductsEntities() : new ProductsEntities(Path.of(databases(), ProductsBoundedDomain.DOMAIN));
        var logic = new ProductsBusinessLogic(realm);
        return new ProductsBoundedDomain(realm, logic, new ProductsHdl(realm, Path.of(imports(), ProductsBoundedDomain.DOMAIN)),
            new ProductsAdapter(logic, Path.of(reports(), ProductsBoundedDomain.DOMAIN)), registry);
    }

    public LedgerBoundedDomain ofLedgerDomain() {
        var realm = (databases() == null) ? new LedgerEntities() : new LedgerEntities(Path.of(databases(), LedgerBoundedDomain.DOMAIN));
        return new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm), new LedgerHdl(realm, Path.of(imports(), LedgerBoundedDomain.DOMAIN)),
            new LedgerAdapter(realm, Path.of(reports(), LedgerBoundedDomain.DOMAIN)), registry);
    }

    private void load() {
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/application.properties"));
        } catch (IOException e) {
            logger.atError().log("Application Configuration Load Error", e);
        }
    }

    private static Subject createAdminSubject() {
        var subject = new Subject();
        ReflectionUtilities.set(subject, HasOid.OID, 900);
        subject.id("administrator");
        subject.newPassword("aeon");
        subject.fromDate(LocalDate.of(2000, Month.JANUARY, 1));
        return subject;
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
