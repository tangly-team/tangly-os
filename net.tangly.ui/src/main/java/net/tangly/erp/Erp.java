package net.tangly.erp;

import java.nio.file.Path;

import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.crm.CrmBusinessLogic;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.invoices.InvoicesBoundedDomain;
import net.tangly.bus.invoices.InvoicesBusinessLogic;
import net.tangly.bus.invoices.InvoicesRealm;
import net.tangly.bus.ledger.LedgerBoundedDomain;
import net.tangly.bus.ledger.LedgerBusinessLogic;
import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.bus.products.ProductsRealm;
import net.tangly.core.TagTypeRegistry;
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
    private static final TagTypeRegistry registry =  new TagTypeRegistry();

    public Erp() {
    }

    public static InvoicesBoundedDomain ofInvoicesDomain() {
        InvoicesRealm realm = new InvoicesEntities(Path.of(ORGANIZATION, "db/invoices/"));
        return new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm), new InvoicesHdl(realm, Path.of(ORGANIZATION, "import/invoices/")),
            new InvoicesAdapter(realm, Path.of(ORGANIZATION, "reports/invoices/")), registry);
    }

    public static CrmBoundedDomain ofCrmDomain() {
        CrmRealm realm = new CrmEntities(Path.of(ORGANIZATION, "db/crm/"));
        return new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmHdl(realm, Path.of(ORGANIZATION, "import/crm/")), null, registry);
    }

    public static ProductsBoundedDomain ofProductsDomain() {
        ProductsRealm realm = new ProductsEntities(Path.of(ORGANIZATION, "db/products/"));
        ProductsBusinessLogic logic = new ProductsBusinessLogic(realm);
        return new ProductsBoundedDomain(realm, logic, new ProductsHdl(realm, Path.of(ORGANIZATION, "import/products/")),
            new ProductsAdapter(logic, Path.of(ORGANIZATION, "reports/assignments")), registry);
    }

    public static LedgerBoundedDomain ofLedgerDomain() {
        LedgerRealm ledger = new LedgerEntities(Path.of(ORGANIZATION, "db/ledger/"));
        return new LedgerBoundedDomain(ledger, new LedgerBusinessLogic(ledger), new LedgerHdl(ledger, Path.of(ORGANIZATION, "import/ledger/")),
            new LedgerAdapter(ledger, Path.of(ORGANIZATION, "reports/ledger")), registry);
    }
}
