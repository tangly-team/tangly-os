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
import java.nio.file.Path;
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
import net.tangly.bus.crm.CrmBusinessLogic;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.invoices.InvoicesBusinessLogic;
import net.tangly.bus.invoices.InvoicesRealm;
import net.tangly.bus.ledger.LedgerBusinessLogic;
import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.bus.products.ProductsRealm;
import net.tangly.commons.bus.ui.TagTypesView;
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
import net.tangly.commons.ledger.ui.AccountsView;
import net.tangly.commons.ledger.ui.TransactionsView;
import net.tangly.commons.products.ui.AssignmentsView;
import net.tangly.commons.products.ui.EffortsView;
import net.tangly.commons.products.ui.ProductsView;
import net.tangly.commons.vaadin.Crud;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import net.tangly.invoices.ports.InvoicesAdapter;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import net.tangly.ledger.ports.LedgerAdapter;
import net.tangly.ledger.ports.LedgerHdl;
import net.tangly.products.ports.ProductsAdapter;
import net.tangly.products.ports.ProductsEntities;
import net.tangly.products.ports.ProductsHdl;
import org.jetbrains.annotations.NotNull;

@Theme(value = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Route("")
public class MainView extends AppLayout {
    private static final String ORGANIZATION = "/Users/Shared/tangly/";
    private Component currentView;

    private CrmBusinessLogic crmLogic;
    private LedgerBusinessLogic ledgerLogic;
    private InvoicesBusinessLogic invoicesLogic;
    private ProductsBusinessLogic productsLogic;

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
    private final AssignmentsView assignmentsView;
    private final EffortsView effortsView;

    private final AccountsView accountsView;
    private final TransactionsView transactionsView;

    private final AnalyticsCrmView analyticsCrmView;

    private final TagTypesView tagTypesView;

    public MainView() {
        importErpData();

        naturalEntitiesView = new NaturalEntitiesView(crmLogic, Crud.Mode.EDITABLE);
        legalEntitiesView = new LegalEntitiesView(crmLogic, Crud.Mode.EDITABLE);
        employeesView = new EmployeesView(crmLogic, Crud.Mode.EDITABLE);
        contractsView = new ContractsView(crmLogic, invoicesLogic, Crud.Mode.EDITABLE);
        interactionsView = new InteractionsView(crmLogic, Crud.Mode.EDITABLE);
        activitiesView = new ActivitiesView(crmLogic, Crud.Mode.READONLY);

        subjectsView = new SubjectsView(crmLogic, Crud.Mode.EDITABLE);

        articlesView = new ArticlesView(invoicesLogic, Crud.Mode.EDITABLE);
        invoicesView = new InvoicesView(invoicesLogic, Crud.Mode.EDITABLE);

        accountsView = new AccountsView(ledgerLogic, Crud.Mode.EDITABLE);
        transactionsView = new TransactionsView(ledgerLogic, Crud.Mode.EDITABLE);

        analyticsCrmView = new AnalyticsCrmView(crmLogic, invoicesLogic, ledgerLogic);
        tagTypesView = new TagTypesView(crmLogic.realm().tagTypeRegistry());

        productsView = new ProductsView(productsLogic, Crud.Mode.EDITABLE);
        assignmentsView = new AssignmentsView(productsLogic, Crud.Mode.EDITABLE);
        effortsView = new EffortsView(productsLogic, Crud.Mode.READONLY);

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

    InvoicesBusinessLogic ofInvoicesLogic(TagTypeRegistry registry) {
        InvoicesRealm realm = new InvoicesEntities(registry);
        return new InvoicesBusinessLogic(realm, new InvoicesHdl(realm, Path.of(ORGANIZATION, "invoices/")),
            new InvoicesAdapter(realm, Path.of(ORGANIZATION, "reports/invoices/")));
    }

    CrmBusinessLogic ofCrmLogic(TagTypeRegistry registry) {
        CrmRealm realm = new CrmEntities(registry);
        return new CrmBusinessLogic(realm, new CrmHdl(realm, Path.of(ORGANIZATION, "crm")), null);
    }

    ProductsBusinessLogic ofProductsLogic(TagTypeRegistry registry) {
        ProductsRealm realm = new ProductsEntities(registry);
        return new ProductsBusinessLogic(realm, new ProductsHdl(realm, Path.of(ORGANIZATION, "products/")),
            new ProductsAdapter(realm, Path.of(ORGANIZATION, "reports/assignments")));
    }

    LedgerBusinessLogic ofLedgerLogic() {
        LedgerRealm ledger = new LedgerRealm();
        return new LedgerBusinessLogic(ledger, new LedgerHdl(ledger, Path.of(ORGANIZATION, "ledger/")),
            new LedgerAdapter(ledger, Path.of(ORGANIZATION, "reports/ledger")));
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
        activitiesSubMenu.addItem("Assignments", e -> select(assignmentsView));
        activitiesSubMenu.addItem("Efforts", e -> select(effortsView));

        MenuItem invoices = menuBar.addItem("Invoices");
        SubMenu invoicesSubMenu = invoices.getSubMenu();
        invoicesSubMenu.addItem("Articles", e -> select(articlesView));
        invoicesSubMenu.addItem("Invoices", e -> select(invoicesView));

        MenuItem ledger = menuBar.addItem("Financials");
        SubMenu ledgerSubMenu = ledger.getSubMenu();
        ledgerSubMenu.addItem("Accounts", e -> select(accountsView));
        ledgerSubMenu.addItem("Transactions", e -> select(transactionsView));
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
        tagTypesView.addCounts(crmLogic.realm().naturalEntities().items());
        tagTypesView.addCounts(crmLogic.realm().legalEntities().items());
        tagTypesView.addCounts(crmLogic.realm().employees().items());
        tagTypesView.addCounts(crmLogic.realm().contracts().items());
        tagTypesView.addCounts(crmLogic.realm().subjects().items());
        tagTypesView.refreshData();
    }

    private void select(@NotNull Component view) {
        setContent(view);
        currentView = view;
    }

    private void importErpData() {
        TagTypeRegistry registry = new TagTypeRegistry();

        crmLogic = ofCrmLogic(registry);
        invoicesLogic = ofInvoicesLogic(registry);
        productsLogic = ofProductsLogic(registry);
        ledgerLogic = ofLedgerLogic();

        crmLogic.registerTags(registry);

        crmLogic.handler().importEntities();
        invoicesLogic.handler().importEntities();
        productsLogic.handler().importEntities();
        ledgerLogic.handler().importEntities();
    }

    private void exportErpData() {
        crmLogic.handler().exportEntities();
        invoicesLogic.handler().exportEntities();
        productsLogic.handler().exportEntities();
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
        assignmentsView.refreshData();
        effortsView.refreshData();
    }
}
