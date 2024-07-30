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

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.tangly.app.ApplicationView;
import net.tangly.app.Tenant;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.core.domain.User;
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
import org.jetbrains.annotations.NotNull;

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
}
