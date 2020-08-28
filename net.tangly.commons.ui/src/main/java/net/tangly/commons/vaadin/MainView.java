/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import java.nio.file.Paths;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import net.tangly.bus.ledger.Ledger;
import net.tangly.commons.bus.ui.TagTypesView;
import net.tangly.commons.crm.ui.ActivitiesView;
import net.tangly.commons.crm.ui.AnalyticsCrmView;
import net.tangly.commons.crm.ui.ContractsView;
import net.tangly.commons.crm.ui.EmployeesView;
import net.tangly.commons.crm.ui.InteractionsView;
import net.tangly.commons.crm.ui.LegalEntitiesView;
import net.tangly.commons.crm.ui.NaturalEntitiesView;
import net.tangly.commons.crm.ui.SubjectsView;
import net.tangly.commons.invoices.ui.InvoicesView;
import net.tangly.commons.invoices.ui.ProductsView;
import net.tangly.commons.ledger.ui.AccountsView;
import net.tangly.commons.ledger.ui.TransactionsView;
import net.tangly.crm.ports.Crm;
import net.tangly.crm.ports.CrmWorkflows;
import net.tangly.ledger.ports.LedgerWorkflows;
import org.jetbrains.annotations.NotNull;

@Theme(value = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Route("")
public class MainView extends VerticalLayout {
    private Component currentView;
    private final Crm crm;
    private final Ledger ledger;
    private final NaturalEntitiesView naturalEntitiesView;
    private final LegalEntitiesView legalEntitiesView;
    private final EmployeesView employeesView;
    private final ContractsView contractsView;

    private final ProductsView productsView;
    private final InvoicesView invoicesView;

    private final InteractionsView interactionsView;
    private final ActivitiesView activitiesView;
    private final SubjectsView subjectsView;

    private final AccountsView accountsView;
    private final TransactionsView transactionsView;

    private final AnalyticsCrmView analyticsCrmView;

    private final TagTypesView tagTypesView;

    public MainView() {
        crm = new Crm();
        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.importCrmEntities(Paths.get("/Users/Shared/tangly/"));

        LedgerWorkflows ledgerWorkflows = new LedgerWorkflows();
        ledgerWorkflows.importLedger(Paths.get("/Users/Shared/tangly/"));
        ledger = ledgerWorkflows.ledger();

        naturalEntitiesView = new NaturalEntitiesView(crm, Crud.Mode.EDITABLE);
        legalEntitiesView = new LegalEntitiesView(crm, Crud.Mode.EDITABLE);
        employeesView = new EmployeesView(crm, Crud.Mode.EDITABLE);
        contractsView = new ContractsView(crm, Crud.Mode.EDITABLE);
        productsView = new ProductsView(crm.products(), Crud.Mode.EDITABLE);
        invoicesView = new InvoicesView(crm.invoices(), Crud.Mode.EDITABLE);
        interactionsView = new InteractionsView(crm, Crud.Mode.EDITABLE);
        activitiesView = new ActivitiesView(crm, Crud.Mode.EDITABLE);
        subjectsView = new SubjectsView(crm, Crud.Mode.EDITABLE);
        accountsView = new AccountsView(ledger, Crud.Mode.EDITABLE);
        transactionsView = new TransactionsView(ledger, Crud.Mode.EDITABLE);
        analyticsCrmView = new AnalyticsCrmView(crm, ledger);
        tagTypesView = new TagTypesView(crm.tagTypeRegistry());

        setSizeFull();
        currentView = naturalEntitiesView;
        add(menuBar(), naturalEntitiesView);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        VaadinUtils.setAttribute(this, "username", "mbaumann");
    }

    private MenuBar menuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);

        MenuItem crm = menuBar.addItem("CRM");
        SubMenu crmSubMenu = crm.getSubMenu();
        crmSubMenu.addItem("Legal Entities", e -> select(legalEntitiesView));
        crmSubMenu.addItem("Natural Entities", e -> select(naturalEntitiesView));
        crmSubMenu.addItem("Contracts", e -> select(contractsView));
        crmSubMenu.addItem("Employees", e -> select(employeesView));
        crmSubMenu.addItem("Interactions", e -> select(interactionsView));
        crmSubMenu.addItem("Activities", e -> select(activitiesView));

        MenuItem activities = menuBar.addItem("Works");
        SubMenu activitiesSubMenu = activities.getSubMenu();
        activitiesSubMenu.addItem("Projects");

        MenuItem invoices = menuBar.addItem("Invoices");
        SubMenu invoicesSubMenu = invoices.getSubMenu();
        invoicesSubMenu.addItem("Products", e -> select(productsView));
        invoicesSubMenu.addItem("Invoices", e -> select(invoicesView));

        MenuItem ledger = menuBar.addItem("Financials");
        SubMenu ledgerSubMenu = ledger.getSubMenu();
        ledgerSubMenu.addItem("Accounts", e -> select(accountsView));
        ledgerSubMenu.addItem("Transactions", e -> select(transactionsView));

        MenuItem analytics = menuBar.addItem("Analytics");
        SubMenu analyticsSubMenu = analytics.getSubMenu();
        analyticsSubMenu.addItem("Turnovers", e -> select(analyticsCrmView));

        MenuItem admin = menuBar.addItem("Admin");
        SubMenu adminSubmenu = admin.getSubMenu();
        adminSubmenu.addItem("Users", e -> select(subjectsView));
        adminSubmenu.addItem("Tags", e -> select(tagTypesView));

        MenuItem actionsItem = adminSubmenu.addItem("Actions");
        SubMenu actions = actionsItem.getSubMenu();
        actions.addItem("Import CRM Data", e -> importCrmData());
        actions.addItem("Export CRM data", e -> exportCrmData());
        actions.addItem("Count CRM Tags", e -> countCrmTags());

        return menuBar;
    }

    private void countCrmTags() {
        tagTypesView.clearCounts();
        tagTypesView.addCounts(crm.naturalEntities().getAll());
        tagTypesView.addCounts(crm.legalEntities().getAll());
        tagTypesView.addCounts(crm.employees().getAll());
        tagTypesView.addCounts(crm.contracts().getAll());
        tagTypesView.addCounts(crm.subjects().getAll());
        tagTypesView.refreshData();
    }

    private void select(@NotNull Component view) {
        this.remove(currentView);
        this.add(view);
        currentView = view;
    }

    private void importCrmData() {
        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.importCrmEntities(Paths.get("/Users/Shared/tangly"));
        refreshViews();
    }

    private void exportCrmData() {
        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.exportCrmEntities(Paths.get("/Users/Shared/tmp"));
    }

    private void refreshViews() {
        naturalEntitiesView.refreshData();
        legalEntitiesView.refreshData();
        employeesView.refreshData();
        contractsView.refreshData();
        productsView.refreshData();
        subjectsView.refreshData();
    }
}
