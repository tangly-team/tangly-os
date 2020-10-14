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
import net.tangly.bus.crm.RealmCrm;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.bus.ledger.Ledger;
import net.tangly.commons.bus.ui.TagTypesView;
import net.tangly.commons.crm.ui.AnalyticsCrmView;
import net.tangly.commons.crm.ui.ContractsView;
import net.tangly.commons.crm.ui.EmployeesView;
import net.tangly.commons.crm.ui.InteractionsView;
import net.tangly.commons.crm.ui.LegalEntitiesView;
import net.tangly.commons.crm.ui.NaturalEntitiesView;
import net.tangly.commons.crm.ui.SubjectsView;
import net.tangly.commons.invoices.ui.ArticlesView;
import net.tangly.commons.invoices.ui.InvoicesView;
import net.tangly.commons.ledger.ui.LedgerView;
import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import net.tangly.ledger.ports.LedgerBusinessLogic;
import net.tangly.ledger.ports.LedgerHdl;
import org.jetbrains.annotations.NotNull;

@Theme(value = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Route("")
public class MainView extends VerticalLayout {
    private Component currentView;
    private final RealmCrm realmCrm;
    private final RealmInvoices realmInvoices;
    private final LedgerBusinessLogic ledgerLogic;
    private final NaturalEntitiesView naturalEntitiesView;
    private final LegalEntitiesView legalEntitiesView;
    private final EmployeesView employeesView;
    private final ContractsView contractsView;

    private final ArticlesView articlesView;
    private final InvoicesView invoicesView;

    private final InteractionsView interactionsView;
    private final SubjectsView subjectsView;

    private final LedgerView ledgerView;

    private final AnalyticsCrmView analyticsCrmView;

    private final TagTypesView tagTypesView;

    public MainView() {
        realmCrm = new CrmEntities();
        realmInvoices = new InvoicesEntities();

        CrmHdl crmHdl = new CrmHdl(realmCrm);
        crmHdl.importEntities(Paths.get("/Users/Shared/tangly/crm"));
        InvoicesHdl invoicesHdl = new InvoicesHdl(realmInvoices);
        invoicesHdl.importEntities(Paths.get("/Users/Shared/tangly/invoices/"));
        LedgerHdl ledgerHdl = new LedgerHdl(new Ledger());
        ledgerHdl.importLedger(Paths.get("/Users/Shared/tangly/ledger"));
        ledgerLogic = new LedgerBusinessLogic(ledgerHdl.ledger());

        naturalEntitiesView = new NaturalEntitiesView(realmCrm, Crud.Mode.EDITABLE);
        legalEntitiesView = new LegalEntitiesView(realmCrm, Crud.Mode.EDITABLE);
        employeesView = new EmployeesView(realmCrm, Crud.Mode.EDITABLE);
        contractsView = new ContractsView(realmCrm, realmInvoices, Crud.Mode.EDITABLE);
        articlesView = new ArticlesView(realmInvoices.articles(), Crud.Mode.EDITABLE);
        invoicesView = new InvoicesView(realmInvoices.invoices(), Crud.Mode.EDITABLE);
        interactionsView = new InteractionsView(realmCrm, Crud.Mode.EDITABLE);
        subjectsView = new SubjectsView(realmCrm, Crud.Mode.EDITABLE);
        ledgerView = new LedgerView(ledgerLogic, Crud.Mode.EDITABLE);
        analyticsCrmView = new AnalyticsCrmView(realmCrm, realmInvoices, ledgerLogic.ledger());
        tagTypesView = new TagTypesView(realmCrm.tagTypeRegistry());

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

        MenuItem activities = menuBar.addItem("Works");
        SubMenu activitiesSubMenu = activities.getSubMenu();
        activitiesSubMenu.addItem("Projects");

        MenuItem invoices = menuBar.addItem("Invoices");
        SubMenu invoicesSubMenu = invoices.getSubMenu();
        invoicesSubMenu.addItem("Products", e -> select(articlesView));
        invoicesSubMenu.addItem("Invoices", e -> select(invoicesView));

        MenuItem ledger = menuBar.addItem("Financials");
        SubMenu ledgerSubMenu = ledger.getSubMenu();
        ledgerSubMenu.addItem("Ledger", e -> select(ledgerView));
        ledgerSubMenu.addItem("Analytics", e -> select(analyticsCrmView));

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
        tagTypesView.addCounts(realmCrm.naturalEntities().items());
        tagTypesView.addCounts(realmCrm.legalEntities().items());
        tagTypesView.addCounts(realmCrm.employees().items());
        tagTypesView.addCounts(realmCrm.contracts().items());
        tagTypesView.addCounts(realmCrm.subjects().items());
        tagTypesView.refreshData();
    }

    private void select(@NotNull Component view) {
        this.remove(currentView);
        this.add(view);
        currentView = view;
    }

    private void importCrmData() {
        CrmHdl crmHdl = new CrmHdl(realmCrm);
        crmHdl.importEntities(Paths.get("/Users/Shared/tangly"));
        refreshViews();
    }

    private void exportCrmData() {
        CrmHdl crmHdl = new CrmHdl(realmCrm);
        crmHdl.exportEntities(Paths.get("/Users/Shared/tmp"));
    }

    private void refreshViews() {
        naturalEntitiesView.refreshData();
        legalEntitiesView.refreshData();
        employeesView.refreshData();
        contractsView.refreshData();
        articlesView.refreshData();
        subjectsView.refreshData();
    }
}
