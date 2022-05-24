/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Crud;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Objects;

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
    private final AnalyticsCrmView analyticsView;
    private final DomainView domainView;
    private Component currentView;

    @Inject
    public CrmBoundedDomainUi(@NotNull CrmBoundedDomain crmDomain, @NotNull InvoicesBoundedDomain invoicesDomain) {
        this.domain = crmDomain;
        leadsView = new LeadsView(domain, Crud.Mode.EDITABLE);
        naturalEntitiesView = new NaturalEntitiesView(domain, Crud.Mode.EDITABLE);
        legalEntitiesView = new LegalEntitiesView(domain, Crud.Mode.EDITABLE);
        employeesView = new EmployeesView(domain, Crud.Mode.EDITABLE);
        contractsView = new ContractsView(domain, Crud.Mode.EDITABLE);
        interactionsView = new InteractionsView(domain, Crud.Mode.EDITABLE);
        activitiesView = new ActivitiesView(domain, Crud.Mode.READONLY);
        subjectsView = new SubjectsView(domain, Crud.Mode.EDITABLE);
        analyticsView = new AnalyticsCrmView(domain, invoicesDomain);
        domainView = new DomainView(domain);
        currentView = naturalEntitiesView;
    }

    @Override
    public String name() {
        return "Customers";
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
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

        addAnalytics(layout, menuBar, analyticsView);
        addAdministration(layout, menuBar, domain, domainView, new CmdFilesUploadCrm(domain));
        select(layout, currentView);
    }

    @Override
    public void select(@NotNull AppLayout layout, Component view) {
        currentView = Objects.isNull(view) ? currentView : view;
        layout.setContent(currentView);
    }
}
