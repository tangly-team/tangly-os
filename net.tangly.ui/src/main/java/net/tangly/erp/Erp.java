package net.tangly.erp;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

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

public class Erp {
    private static final String ORGANIZATION = "/Users/Shared/tangly/";
    private static final TypeRegistry registry = new TypeRegistry();

    // TODO configure root directory through parameter
    public Erp() {
    }

    public static InvoicesBoundedDomain ofInvoicesDomain() {
        // TODO change as soon as MicroStream supports again records, currently persistence is disabled
        //        var realm = new InvoicesEntities(Path.of(ORGANIZATION, "db/invoices/"));
        var realm = new InvoicesEntities();
        return new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm), new InvoicesHdl(realm, Path.of(ORGANIZATION, "import/invoices/")),
            new InvoicesAdapter(realm, Path.of(ORGANIZATION, "reports/invoices/")), registry);
    }

    public static CrmBoundedDomain ofCrmDomain() {
        // TODO change as soon as MicroStream supports again records, currently persistence is disabled
        //        var realm = new CrmEntities(Path.of(ORGANIZATION, "db/crm/"));
        var realm = new CrmEntities();
        if (realm.subjects().items().isEmpty()) {
            realm.subjects().update(createAdminSubject());
        }
        return new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmHdl(realm, Path.of(ORGANIZATION, "import/crm/")), null, registry);
    }

    public static ProductsBoundedDomain ofProductsDomain() {
        // TODO change as soon as MicroStream supports again records, currently persistence is disabled
        //        var realm = new ProductsEntities(Path.of(ORGANIZATION, "db/products/"));
        var realm = new ProductsEntities();
        var logic = new ProductsBusinessLogic(realm);
        return new ProductsBoundedDomain(realm, logic, new ProductsHdl(realm, Path.of(ORGANIZATION, "import/products/")),
            new ProductsAdapter(logic, Path.of(ORGANIZATION, "reports/assignments")), registry);
    }

    public static LedgerBoundedDomain ofLedgerDomain() {
        // TODO change as soon as MicroStream supports again records, currently persistence is disabled
        //        var ledger = new LedgerEntities(Path.of(ORGANIZATION, "db/ledger/"));
        var ledger = new LedgerEntities();
        return new LedgerBoundedDomain(ledger, new LedgerBusinessLogic(ledger), new LedgerHdl(ledger, Path.of(ORGANIZATION, "import/ledger/")),
            new LedgerAdapter(ledger, Path.of(ORGANIZATION, "reports/ledger")), registry);
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
