/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.crm.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.invoices.InvoicesBoundedDomain;
import net.tangly.commons.domain.ui.CmdExportEntities;
import net.tangly.commons.domain.ui.CmdImportEntities;
import net.tangly.commons.domain.ui.DomainView;
import net.tangly.commons.ui.BoundedDomainUi;
import net.tangly.commons.ui.MainLayout;
import net.tangly.commons.vaadin.Crud;
import org.jetbrains.annotations.NotNull;

public class CrmBoundedDomainUi implements BoundedDomainUi {
    private final CrmBoundedDomain domain;
    private final LeadsView leadsView;
    private final NaturalEntitiesView naturalEntitiesView;
    private final LegalEntitiesView legalEntitiesView;
    private final EmployeesView employeesView;
    private final ContractsView contractsView;
    private final InteractionsView interactionsView;
    private final ActivitiesView activitiesView;
    private final SubjectsView subjectsView;
    private final DomainView domainView;
    private Component currentView;

    @Inject
    public CrmBoundedDomainUi(@NotNull CrmBoundedDomain crmDomain, @NotNull InvoicesBoundedDomain invoicesDomain) {
        this.domain = crmDomain;
        leadsView = new LeadsView(crmDomain, Crud.Mode.EDITABLE);
        naturalEntitiesView = new NaturalEntitiesView(crmDomain, Crud.Mode.EDITABLE);
        legalEntitiesView = new LegalEntitiesView(crmDomain, Crud.Mode.EDITABLE);
        employeesView = new EmployeesView(crmDomain, Crud.Mode.EDITABLE);
        contractsView = new ContractsView(crmDomain, invoicesDomain.logic(), Crud.Mode.EDITABLE);
        interactionsView = new InteractionsView(crmDomain, Crud.Mode.EDITABLE);
        activitiesView = new ActivitiesView(crmDomain, Crud.Mode.READONLY);
        subjectsView = new SubjectsView(crmDomain, Crud.Mode.EDITABLE);
        domainView = new DomainView(crmDomain);
        currentView = naturalEntitiesView;
    }

    @Override
    public String name() {
        return "Customers";
    }

    @Override
    public void select(@NotNull MainLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Leads", e -> select(layout, leadsView));
        subMenu.addItem("Legal Entities", e -> select(layout, legalEntitiesView));
        subMenu.addItem("Natural Entities", e -> select(layout, naturalEntitiesView));
        subMenu.addItem("Contracts", e -> select(layout, contractsView));
        subMenu.addItem("Employees", e -> select(layout, employeesView));
        subMenu.addItem("Interactions", e -> select(layout, interactionsView));
        subMenu.addItem("Activities", e -> select(layout, activitiesView));
        subMenu.addItem("Subjects", e -> select(layout, subjectsView));

        menuItem = menuBar.addItem(ADMINISTRATION);
        subMenu = menuItem.getSubMenu();
        subMenu.addItem(STATISTICS, e -> select(layout, domainView));
        subMenu.addItem(IMPORT, e -> new CmdImportEntities(domain).execute());
        subMenu.addItem(EXPORT, e -> new CmdExportEntities(domain).execute());
        select(layout, currentView);
    }

    private void select(@NotNull MainLayout layout, @NotNull Component view) {
        layout.setContent(view);
        currentView = view;
    }
}
