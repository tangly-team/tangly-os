package net.tangly.erp;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.Properties;

import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.crm.CrmBusinessLogic;
import net.tangly.bus.crm.Subject;
import net.tangly.bus.invoices.InvoicesBoundedDomain;
import net.tangly.bus.invoices.InvoicesBusinessLogic;
import net.tangly.bus.ledger.LedgerBoundedDomain;
import net.tangly.bus.ledger.LedgerBusinessLogic;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.core.HasOid;
import net.tangly.core.TypeRegistry;
import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import net.tangly.invoices.ports.InvoicesAdapter;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import net.tangly.ledger.ports.LedgerAdapter;
import net.tangly.ledger.ports.LedgerEntities;
import net.tangly.ledger.ports.LedgerHdl;
import net.tangly.products.ports.ProductsAdapter;
import net.tangly.products.ports.ProductsEntities;
import net.tangly.products.ports.ProductsHdl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ERP application instantiating the bounded domain instances. The class implements a modular monolith application.
 */
public class Erp {
    private static final String ROOT_DIRECTORY_PROPERTY = "erp.root.directory";
    private static final String DATABASES_DIRECTORY_PROPERTY = "erp.root.db.directory";
    private static final String IMPORTS_DIRECTORY_PROPERTY = "erp.root.imports.directory";
    private static final String REPORTS_DIRECTORY_PROPERTY = "erp.root.reports.directory";

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final TypeRegistry registry;
    private final String organization;
    private final String databases;
    private final String imports;
    private final String reports;
    private final Properties properties;

    public Erp() {
        this.registry = new TypeRegistry();
        this.properties = new Properties();
        load();
        organization = properties.getProperty(ROOT_DIRECTORY_PROPERTY);
        databases = properties.getProperty(DATABASES_DIRECTORY_PROPERTY);
        imports = properties.getProperty(IMPORTS_DIRECTORY_PROPERTY);
        reports = properties.getProperty(REPORTS_DIRECTORY_PROPERTY);

    }

    public InvoicesBoundedDomain ofInvoicesDomain() {
        var realm = (databases == null) ? new InvoicesEntities() : new InvoicesEntities(Path.of(databases, InvoicesBoundedDomain.DOMAIN));
        return new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm), new InvoicesHdl(realm, Path.of(imports, InvoicesBoundedDomain.DOMAIN)),
            new InvoicesAdapter(realm, Path.of(reports, InvoicesBoundedDomain.DOMAIN)), registry);
    }

    public CrmBoundedDomain ofCrmDomain() {
        var realm = (databases == null) ? new CrmEntities() : new CrmEntities(Path.of(databases, CrmBoundedDomain.DOMAIN));
        if (realm.subjects().items().isEmpty()) {
            realm.subjects().update(createAdminSubject());
        }
        return new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmHdl(realm, Path.of(imports, CrmBoundedDomain.DOMAIN)), null, registry);
    }

    public ProductsBoundedDomain ofProductsDomain() {
        var realm = (databases == null) ? new ProductsEntities() : new ProductsEntities(Path.of(databases, ProductsBoundedDomain.DOMAIN));
        var logic = new ProductsBusinessLogic(realm);
        return new ProductsBoundedDomain(realm, logic, new ProductsHdl(realm, Path.of(imports, ProductsBoundedDomain.DOMAIN)),
            new ProductsAdapter(logic, Path.of(reports, ProductsBoundedDomain.DOMAIN)), registry);
    }

    public LedgerBoundedDomain ofLedgerDomain() {
        var realm = (databases == null) ? new LedgerEntities() : new LedgerEntities(Path.of(databases, LedgerBoundedDomain.DOMAIN));
        return new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm), new LedgerHdl(realm, Path.of(imports, LedgerBoundedDomain.DOMAIN)),
            new LedgerAdapter(realm, Path.of(reports, LedgerBoundedDomain.DOMAIN)), registry);
    }

    private void load() {
        String propertiesPath = Thread.currentThread().getContextClassLoader().getResource("application.properties").getPath();
        try (var propertiesFile = new FileInputStream(propertiesPath)) {
            properties.load(propertiesFile);
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
}
