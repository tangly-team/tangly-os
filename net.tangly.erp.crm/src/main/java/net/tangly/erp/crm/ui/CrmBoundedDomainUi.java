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

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class CrmBoundedDomainUi extends BoundedDomainUi<CrmBoundedDomain> {
    private final LazyReference<LeadsView> leadsView;
    private final LazyReference<NaturalEntitiesView> naturalEntitiesView;
    private final LazyReference<LegalEntitiesView> legalEntitiesView;
    private final LazyReference<EmployeesView> employeesView;
    private final LazyReference<ContractsView> contractsView;
    private final LazyReference<InteractionsView> interactionsView;
    private final LazyReference<ActivitiesView> activitiesView;
    private final LazyReference<AnalyticsCrmView> analyticsView;
    private final LazyReference<DomainView> domainView;

    public CrmBoundedDomainUi(@NotNull CrmBoundedDomain crmDomain, @NotNull InvoicesBoundedDomain invoicesDomain) {
        super(crmDomain);
        leadsView = new LazyReference<>(() -> new LeadsView(domain(), Mode.EDIT));
        naturalEntitiesView = new LazyReference<>(() -> new NaturalEntitiesView(domain(), Mode.EDIT));
        legalEntitiesView = new LazyReference<>(() -> new LegalEntitiesView(domain(), Mode.EDIT));
        employeesView = new LazyReference<>(() -> new EmployeesView(domain(), Mode.EDIT));
        contractsView = new LazyReference<>(() -> new ContractsView(domain(), Mode.EDIT));
        interactionsView = new LazyReference<>(() -> new InteractionsView(domain(), Mode.EDIT));
        activitiesView = new LazyReference<>(() -> new ActivitiesView(domain(), Mode.VIEW));
        analyticsView = new LazyReference<>(() -> new AnalyticsCrmView(domain(), invoicesDomain));
        domainView = new LazyReference<>(() -> new DomainView(domain()));
        currentView(naturalEntitiesView);
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Leads", e -> select(layout, leadsView));
        subMenu.addItem("Legal Entities", e -> select(layout, legalEntitiesView));
        subMenu.addItem("Natural Entities", e -> select(layout, naturalEntitiesView));
        subMenu.addItem("Contracts", e -> select(layout, contractsView));
        subMenu.addItem("Employees", e -> select(layout, employeesView));
        subMenu.addItem("Interactions", e -> select(layout, interactionsView));
        subMenu.addItem("Activities", e -> select(layout, activitiesView));

        addAnalytics(layout, menuBar, analyticsView);
        addAdministration(layout, menuBar, domainView, new CmdFilesUploadCrm(domain()));
        select(layout);
    }

    @Override
    public void refreshViews() {
        leadsView.ifPresent(ItemView::refresh);
        naturalEntitiesView.ifPresent(ItemView::refresh);
        legalEntitiesView.ifPresent(ItemView::refresh);
        employeesView.ifPresent(ItemView::refresh);
        contractsView.ifPresent(ItemView::refresh);
        interactionsView.ifPresent(ItemView::refresh);
        activitiesView.ifPresent(ItemView::refresh);
    }
}
