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

import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.tangly.app.ApplicationView;
import net.tangly.app.Tenant;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.core.domain.User;
import net.tangly.core.providers.Provider;
import net.tangly.erp.collaborators.ui.CollaboratorsBoundedDomainUi;
import net.tangly.erp.collabortors.services.CollaboratorsBoundedDomain;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.ui.CrmBoundedDomainUi;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.ui.InvoicesBoundedDomainUi;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.ui.LedgerBoundedDomainUi;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.ui.ProductsBoundedDomainUi;
import net.tangly.ui.app.domain.BoundedDomainUi;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@PageTitle("tangly ERP")
@Route("")
public class MainView extends ApplicationView {
    public MainView() {
        super(null, true);
    }

    @Override
    public void userChanged(@NotNull Tenant tenant, @NotNull User user) {
        super.userChanged(tenant, user);
        ofDomainUis(tenant);
        drawerMenu(user);
        selectBoundedDomainUi(AppsBoundedDomain.DOMAIN);
    }

    private void ofDomainUis(@NotNull Tenant tenant) {
        var crmBoundedDomain = tenant.getBoundedDomain(CrmBoundedDomain.DOMAIN);
        var invoicesBoundedDomain = tenant.getBoundedDomain(InvoicesBoundedDomain.DOMAIN);
        if (crmBoundedDomain.isPresent() && invoicesBoundedDomain.isPresent()) {
            registerBoundedDomainUi(new CrmBoundedDomainUi((CrmBoundedDomain) crmBoundedDomain.get(), (InvoicesBoundedDomain) invoicesBoundedDomain.get()));
        }
        tenant.getBoundedDomain(ProductsBoundedDomain.DOMAIN).ifPresent(o -> registerBoundedDomainUi(new ProductsBoundedDomainUi((ProductsBoundedDomain) o)));
        tenant.getBoundedDomain(InvoicesBoundedDomain.DOMAIN).ifPresent(o -> registerBoundedDomainUi(new InvoicesBoundedDomainUi((InvoicesBoundedDomain) o)));
        tenant.getBoundedDomain(LedgerBoundedDomain.DOMAIN).ifPresent(o -> registerBoundedDomainUi(new LedgerBoundedDomainUi((LedgerBoundedDomain) o)));
        tenant.getBoundedDomain(CollaboratorsBoundedDomain.DOMAIN)
            .ifPresent(o -> registerBoundedDomainUi(new CollaboratorsBoundedDomainUi((CollaboratorsBoundedDomain) o)));
    }


    /**
     * application administrators have access to the global housekeeping functions of the application.
     *
     * @param ui bounded domain user interface
     */
    @Override
    protected void selectBoundedDomainUi(@NotNull BoundedDomainUi<?> ui) {
        super.selectBoundedDomainUi(ui);
        var menuItem = menuBar().addItem("Housekeeping");
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Setup All", _ -> {
            exportDocuments();
            boundedDomainUis().values().forEach(BoundedDomainUi::refreshViews);
        });
    }

    private void exportDocuments() {
        var invoices = (InvoicesBoundedDomain) (tenant().getBoundedDomain(InvoicesBoundedDomain.DOMAIN).orElseThrow());
        invoices.port().exportInvoiceDocuments(invoices, false, false, true, LocalDate.of(2015, Month.JANUARY, 1), LocalDate.of(2024, Month.DECEMBER, 31),
            Collections.emptyList());
        var ledger = (LedgerBoundedDomain) (tenant().getBoundedDomain(LedgerBoundedDomain.DOMAIN).orElseThrow());
        ledger.port()
            .exportLedgerDocument("tangly-2016", LocalDate.of(2015, Month.SEPTEMBER, 1), LocalDate.of(2016, Month.DECEMBER, 31), true, true, false, true, true,
                null, Collections.emptyList(), ledger);
        for (int year = 2017; year <= 2024; year++) {
            ledger.port().exportLedgerDocument("tangly-%s".formatted(Integer.toString(year)), LocalDate.of(year, Month.JANUARY, 1),
                LocalDate.of(year, Month.DECEMBER, 31), true, true, false, true, true, null, Collections.emptyList(), ledger);
        }
        var products = (ProductsBoundedDomain) (tenant().getBoundedDomain(ProductsBoundedDomain.DOMAIN).orElseThrow());
        var assignment = Provider.findByOid(products.realm().assignments(), 40002).orElseThrow();
        products.port().exportEffortsDocument(products, assignment, LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2024, Month.MARCH, 31),
            "2024-03-PT24-marcel-baumann-WorkReport-March", ChronoUnit.HOURS);
        products.port().exportEffortsDocument(products, assignment, LocalDate.of(2024, Month.APRIL, 1), LocalDate.of(2024, Month.APRIL, 30),
            "2024-04-PT24-marcel-baumann-WorkReport-April", ChronoUnit.HOURS);
        assignment = Provider.findByOid(products.realm().assignments(), 40004).orElseThrow();
        products.port().exportEffortsDocument(products, assignment, LocalDate.of(2024, Month.JUNE, 1), LocalDate.of(2024, Month.AUGUST, 31),
            "2024-08-DTV24-marcel-baumann-WorkReport-August", ChronoUnit.HOURS);
    }
}
