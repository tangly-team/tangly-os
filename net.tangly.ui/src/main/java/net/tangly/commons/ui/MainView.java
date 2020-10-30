/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Objects;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.BusinessLogicCrm;
import net.tangly.bus.crm.RealmCrm;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.bus.ledger.BusinessLogicLedger;
import net.tangly.bus.ledger.Ledger;
import net.tangly.bus.products.RealmProducts;
import net.tangly.commons.bus.ui.TagTypesView;
import net.tangly.commons.crm.products.ui.AssignementsView;
import net.tangly.commons.crm.products.ui.EffortsView;
import net.tangly.commons.crm.products.ui.ProductsView;
import net.tangly.commons.crm.ui.ActivitiesView;
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
import net.tangly.commons.vaadin.Crud;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import net.tangly.ledger.ports.LedgerPort;
import net.tangly.ledger.ports.LedgerHdl;
import net.tangly.products.ports.ProductsEntities;
import net.tangly.products.ports.ProductsHdl;
import org.jetbrains.annotations.NotNull;

@Theme(value = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Route("")
public class MainView extends AppLayout {
    private Component currentView;
    private RealmCrm realmCrm;
    private RealmInvoices realmInvoices;
    private RealmProducts realmProducts;
    private BusinessLogicLedger ledgerLogic;

    private final NaturalEntitiesView naturalEntitiesView;
    private final LegalEntitiesView legalEntitiesView;
    private final EmployeesView employeesView;
    private final ContractsView contractsView;
    private final InteractionsView interactionsView;
    private final ActivitiesView activitiesView;
    private final SubjectsView subjectsView;

    private final ArticlesView articlesView;
    private final InvoicesView invoicesView;

    private final ProductsView productsView;
    private final AssignementsView assignementsView;
    private final EffortsView effortsView;

    private final LedgerView ledgerView;

    private final AnalyticsCrmView analyticsCrmView;

    private final TagTypesView tagTypesView;

    public MainView() {
        importErpData();

        naturalEntitiesView = new NaturalEntitiesView(realmCrm, Crud.Mode.EDITABLE);
        legalEntitiesView = new LegalEntitiesView(realmCrm, Crud.Mode.EDITABLE);
        employeesView = new EmployeesView(realmCrm, Crud.Mode.EDITABLE);
        contractsView = new ContractsView(realmCrm, realmInvoices, Crud.Mode.EDITABLE);
        interactionsView = new InteractionsView(realmCrm, Crud.Mode.EDITABLE);
        activitiesView = new ActivitiesView(realmCrm, Crud.Mode.READONLY);

        subjectsView = new SubjectsView(realmCrm, Crud.Mode.EDITABLE);

        articlesView = new ArticlesView(realmInvoices.articles(), Crud.Mode.EDITABLE);
        invoicesView = new InvoicesView(realmInvoices.invoices(), Crud.Mode.EDITABLE);

        ledgerView = new LedgerView(ledgerLogic, Crud.Mode.EDITABLE);

        analyticsCrmView = new AnalyticsCrmView(realmCrm, realmInvoices, ledgerLogic.ledger());
        tagTypesView = new TagTypesView(realmCrm.tagTypeRegistry());

        productsView = new ProductsView(realmProducts, Crud.Mode.EDITABLE);
        assignementsView = new AssignementsView(realmProducts, Crud.Mode.EDITABLE);
        effortsView = new EffortsView(realmProducts, Crud.Mode.READONLY);

        currentView = naturalEntitiesView;

        Image image;
        try {
            byte[] buffer = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tangly70x70.png")).readAllBytes();
            image = new Image(new StreamResource("tangly70x70.png", () -> new ByteArrayInputStream(buffer)), "tangly70x70.png");
            image.setHeight("44px");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        setPrimarySection(Section.NAVBAR);
        addToNavbar(new DrawerToggle(), image, menuBar());
        setContent(naturalEntitiesView);
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
        activitiesSubMenu.addItem("Products", e -> select(productsView));
        activitiesSubMenu.addItem("Assignments", e -> select(assignementsView));
        activitiesSubMenu.addItem("Efforts", e -> select(effortsView));

        MenuItem invoices = menuBar.addItem("Invoices");
        SubMenu invoicesSubMenu = invoices.getSubMenu();
        invoicesSubMenu.addItem("Articles", e -> select(articlesView));
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
        actions.addItem("Import CRM Data", e -> {
            importErpData();
            refreshViews();
        });
        actions.addItem("Export CRM data", e -> exportErpData());
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
        setContent(view);
        currentView = view;
    }

    private void importErpData() {
        TagTypeRegistry registry = new TagTypeRegistry();

        realmCrm = new CrmEntities(registry);
        realmInvoices = new InvoicesEntities(registry);
        realmProducts = new ProductsEntities(registry);

        BusinessLogicCrm businessLogicCrm = new BusinessLogicCrm(realmCrm);
        businessLogicCrm.registerTags(registry);

        CrmHdl crmHdl = new CrmHdl(realmCrm, Paths.get("/Users/Shared/tangly/crm"));
        crmHdl.importEntities();

        InvoicesHdl invoicesHdl = new InvoicesHdl(realmInvoices, Paths.get("/Users/Shared/tangly/invoices"));
        invoicesHdl.importEntities();

        ProductsHdl productsHdl = new ProductsHdl(realmProducts, Paths.get("/Users/Shared/tangly/products/"));
        productsHdl.importEntities();

        LedgerHdl ledgerHdl = new LedgerHdl(new Ledger(), Paths.get("/Users/Shared/tangly/ledger"));
        ledgerHdl.importEntities();
        ledgerLogic = new BusinessLogicLedger(ledgerHdl.ledger());
    }

    private void exportErpData() {
        CrmHdl crmHdl = new CrmHdl(realmCrm, Paths.get("/Users/Shared/tangly/crm"));
        crmHdl.exportEntities();
        InvoicesHdl invoicesHdl = new InvoicesHdl(realmInvoices, Paths.get("/Users/Shared/tangly/invoices"));
        invoicesHdl.exportEntities();
    }

    private void refreshViews() {
        naturalEntitiesView.refreshData();
        legalEntitiesView.refreshData();
        employeesView.refreshData();
        contractsView.refreshData();
        articlesView.refreshData();
        subjectsView.refreshData();

        articlesView.refreshData();
        invoicesView.refreshData();

        productsView.refreshData();
        assignementsView.refreshData();
        effortsView.refreshData();
    }
}
