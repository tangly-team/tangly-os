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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.tangly.app.Application;
import net.tangly.app.ApplicationView;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.core.Entity;
import net.tangly.core.HasOid;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmEntities;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.crm.ui.CmdLogin;
import net.tangly.erp.crm.ui.CrmBoundedDomainUi;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import net.tangly.erp.invoices.ui.InvoicesBoundedDomainUi;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import net.tangly.erp.ledger.ui.LedgerBoundedDomainUi;
import net.tangly.erp.products.ports.ProductsAdapter;
import net.tangly.erp.products.ports.ProductsEntities;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erp.products.ui.ProductsBoundedDomainUi;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

@PageTitle("tangly ERP")
@Route("")
public class MainView extends ApplicationView {
    private static final String IMAGE_NAME = "tangly70x70.png";

    public MainView() {
        ofDomains(Application.instance());
        ofDomainUis(Application.instance());
        super(IMAGE_NAME);
        selectBoundedDomainUi(CrmBoundedDomain.DOMAIN);
    }

    public static Application create() {
        ofDomains(Application.instance());
        return Application.instance();
    }

    public static CrmBoundedDomain crmBoundedDomain() {
        return (CrmBoundedDomain) Application.instance().getBoundedDomain(CrmBoundedDomain.DOMAIN).orElseThrow();
    }

    @Override
    protected void onAttach(@NotNull AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (Objects.isNull(VaadinUtils.getAttribute(this, "subject")) && Objects.nonNull(crmBoundedDomain())) {
            new CmdLogin(crmBoundedDomain()).execute();
        }
    }

    private static void ofDomains(Application application) {
        if (application.isEnabled(CrmBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new CrmEntities() : new CrmEntities(Path.of(application.databases(), CrmBoundedDomain.DOMAIN));
            if (realm.subjects().items().isEmpty()) {
                realm.subjects().update(createAdminSubject());
            }
            var domain = new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmAdapter(realm, Path.of(Application.instance().imports(CrmBoundedDomain.DOMAIN))),
                application.registry());
            application.registerBoundedDomain(domain);
        }
        if (application.isEnabled(InvoicesBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new InvoicesEntities() : new InvoicesEntities(Path.of(application.databases(), InvoicesBoundedDomain.DOMAIN));
            var domain = new InvoicesBoundedDomain(realm, new InvoicesBusinessLogic(realm), new InvoicesAdapter(realm, Path.of(application.reports(InvoicesBoundedDomain.DOMAIN))),
                application.registry());
            application.registerBoundedDomain(domain);
        }
        if (application.isEnabled(LedgerBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new LedgerEntities() : new LedgerEntities(Path.of(application.databases(), LedgerBoundedDomain.DOMAIN));
            var domain = new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm), new LedgerAdapter(realm, Path.of(Application.instance().imports(LedgerBoundedDomain.DOMAIN)),
                Path.of(application.reports(LedgerBoundedDomain.DOMAIN))), application.registry());
            application.registerBoundedDomain(domain);
        }
        if (application.isEnabled(ProductsBoundedDomain.DOMAIN)) {
            var realm = application.inMemory() ? new ProductsEntities() : new ProductsEntities(Path.of(application.databases(), ProductsBoundedDomain.DOMAIN));
            var logic = new ProductsBusinessLogic(realm);
            var domain = new ProductsBoundedDomain(realm, logic, new ProductsAdapter(realm, logic, Path.of(Application.instance().imports(ProductsBoundedDomain.DOMAIN)),
                Path.of(application.reports(ProductsBoundedDomain.DOMAIN))), application.registry());
            application.registerBoundedDomain(domain);
        }
    }

    private static void ofDomainUis(Application application) {
        var crmBoundedDomain = application.getBoundedDomain(CrmBoundedDomain.DOMAIN);
        var invoicesBoundedDomain = application.getBoundedDomain(InvoicesBoundedDomain.DOMAIN);
        if (crmBoundedDomain.isPresent() && invoicesBoundedDomain.isPresent()) {
            application.registerBoundedDomainUi(new CrmBoundedDomainUi((CrmBoundedDomain) crmBoundedDomain.get(), (InvoicesBoundedDomain) invoicesBoundedDomain.get()));
        }
        application.getBoundedDomain(ProductsBoundedDomain.DOMAIN).ifPresent(o -> application.registerBoundedDomainUi(new ProductsBoundedDomainUi((ProductsBoundedDomain) o)));
        application.getBoundedDomain(InvoicesBoundedDomain.DOMAIN).ifPresent(o -> application.registerBoundedDomainUi(new InvoicesBoundedDomainUi((InvoicesBoundedDomain) o)));
        application.getBoundedDomain(LedgerBoundedDomain.DOMAIN).ifPresent(o -> application.registerBoundedDomainUi(new LedgerBoundedDomainUi((LedgerBoundedDomain) o)));

    }

    private static Subject createAdminSubject() {
        var subject = new Subject(Entity.UNDEFINED_OID);
        ReflectionUtilities.set(subject, HasOid.OID, 900);
        subject.id("aeon");
        subject.newPassword("aeon");
        subject.from(LocalDate.of(2000, Month.JANUARY, 1));
        return subject;
    }
}
