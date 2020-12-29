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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.crm.Subject;
import net.tangly.bus.invoices.InvoicesBoundedDomain;
import net.tangly.bus.ledger.LedgerBoundedDomain;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.commons.crm.ui.ActivitiesView;
import net.tangly.commons.crm.ui.AnalyticsCrmView;
import net.tangly.commons.crm.ui.ContractsView;
import net.tangly.commons.crm.ui.EmployeesView;
import net.tangly.commons.crm.ui.InteractionsView;
import net.tangly.commons.crm.ui.LegalEntitiesView;
import net.tangly.commons.crm.ui.NaturalEntitiesView;
import net.tangly.commons.crm.ui.SubjectsView;
import net.tangly.commons.domain.ui.DomainView;
import net.tangly.commons.invoices.ui.ArticlesView;
import net.tangly.commons.invoices.ui.InvoicesView;
import net.tangly.commons.ledger.ui.AccountsView;
import net.tangly.commons.ledger.ui.TransactionsView;
import net.tangly.commons.products.ui.AssignmentsView;
import net.tangly.commons.products.ui.EffortsView;
import net.tangly.commons.products.ui.ProductsView;
import net.tangly.commons.vaadin.Crud;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.erp.Erp;
import org.jetbrains.annotations.NotNull;

@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/override-overlay.css", themeFor = "vaadin-dialog-overlay")
@CssImport(value = "./styles/override-negative.css", themeFor = "vaadin-grid")
@JsModule("prefers-color-scheme.js")
@PageTitle("tangly ERP")
@Route("")
public class MainView extends AppLayout {
    private Component currentView;

    private static final CrmBoundedDomain crmDomain;
    private static final LedgerBoundedDomain ledgerDomain;
    private static final InvoicesBoundedDomain invoicesDomain;
    private static final ProductsBoundedDomain productsDomain;

    private final NaturalEntitiesView naturalEntitiesView;
    private final LegalEntitiesView legalEntitiesView;
    private final EmployeesView employeesView;
    private final ContractsView contractsView;
    private final InteractionsView interactionsView;
    private final ActivitiesView activitiesView;
    private final SubjectsView subjectsView;
    private final DomainView crmDomainView;

    private final ArticlesView articlesView;
    private final InvoicesView invoicesView;
    private final DomainView invoicesDomainView;

    private final ProductsView productsView;
    private final AssignmentsView assignmentsView;
    private final EffortsView effortsView;
    private final DomainView productsDomainView;

    private final AccountsView accountsView;
    private final TransactionsView transactionsView;
    private final DomainView ledgerDomainView;

    private final AnalyticsCrmView analyticsCrmView;

    static {
        crmDomain = Erp.ofCrmDomain();
        invoicesDomain = Erp.ofInvoicesDomain();
        productsDomain = Erp.ofProductsDomain();
        ledgerDomain = Erp.ofLedgerDomain();
    }

    public MainView() {
        naturalEntitiesView = new NaturalEntitiesView(crmDomain, Crud.Mode.EDITABLE);
        legalEntitiesView = new LegalEntitiesView(crmDomain, Crud.Mode.EDITABLE);
        employeesView = new EmployeesView(crmDomain, Crud.Mode.EDITABLE);
        contractsView = new ContractsView(crmDomain, invoicesDomain.logic(), Crud.Mode.EDITABLE);
        interactionsView = new InteractionsView(crmDomain, Crud.Mode.EDITABLE);
        activitiesView = new ActivitiesView(crmDomain, Crud.Mode.READONLY);
        subjectsView = new SubjectsView(crmDomain, Crud.Mode.EDITABLE);
        crmDomainView = new DomainView(crmDomain);

        articlesView = new ArticlesView(invoicesDomain, Crud.Mode.EDITABLE);
        invoicesView = new InvoicesView(invoicesDomain, Crud.Mode.EDITABLE);
        invoicesDomainView = new DomainView(invoicesDomain);

        accountsView = new AccountsView(ledgerDomain, Crud.Mode.EDITABLE);
        transactionsView = new TransactionsView(ledgerDomain, Crud.Mode.EDITABLE);
        ledgerDomainView = new DomainView(ledgerDomain);

        productsView = new ProductsView(productsDomain, Crud.Mode.EDITABLE);
        assignmentsView = new AssignmentsView(productsDomain, Crud.Mode.EDITABLE);
        effortsView = new EffortsView(productsDomain, Crud.Mode.EDIT_DELETE);
        productsDomainView = new DomainView(productsDomain);

        analyticsCrmView = new AnalyticsCrmView(crmDomain.logic(), invoicesDomain.logic(), ledgerDomain.logic());

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
    protected void onAttach(@NotNull AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (Objects.isNull(VaadinUtils.getAttribute(this, "subject"))) {
            LoginOverlay component = new LoginOverlay();
            LoginI18n i18n = LoginI18n.createDefault();
            i18n.setHeader(new LoginI18n.Header());
            i18n.getHeader().setTitle("tangly ERP");
            i18n.getHeader().setDescription("tangly llc ERP Application");
            component.setI18n(i18n);
            component.setOpened(true);
            component.addLoginListener(e -> {
                Optional<Subject> subject = crmDomain.logic().login(e.getUsername(), e.getPassword());
                if (subject.isPresent()) {
                    VaadinUtils.setAttribute(this, "subject", subject.get());
                    VaadinUtils.setAttribute(this, "username", subject.get().id());
                    component.close();
                } else {
                    component.setError(true);
                }
            });
        }
    }

    public void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
        UI.getCurrent().getPage().setLocation("");
    }

    private void registerDomain(@NotNull MenuBar menuBar, BoundedDomain<?, ?, ?, ?> boundedDomain, String domainName, Consumer<SubMenu> registerViews,
                                Consumer<SubMenu> registerAnalyticsViews, Consumer<SubMenu> registerAdministrationViews) {
        SubMenu domainMenu = menuBar.addItem(domainName).getSubMenu();
        registerViews.accept(domainMenu);
        domainMenu.addItem("Legal Entities", e -> select(legalEntitiesView));
        domainMenu.addItem("Natural Entities", e -> select(naturalEntitiesView));
        domainMenu.addItem("Contracts", e -> select(contractsView));
        domainMenu.addItem("Employees", e -> select(employeesView));
        domainMenu.addItem("Interactions", e -> select(interactionsView));
        domainMenu.addItem("Activities", e -> select(activitiesView));

        SubMenu analyticsSubMenu = domainMenu.addItem("Analytics").getSubMenu();
        registerAnalyticsViews.accept(analyticsSubMenu);

        SubMenu adminSubMenu = domainMenu.addItem("Administration").getSubMenu();
        registerAdministrationViews.accept(adminSubMenu);

        adminSubMenu.addItem("Import Data", e -> {
            boundedDomain.handler().importEntities();
            refreshViews();
        });
        adminSubMenu.addItem("Export Data", e -> boundedDomain.handler().exportEntities());
    }

    private MenuBar menuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);

        MenuItem crm = menuBar.addItem("Customers");
        SubMenu crmSubMenu = crm.getSubMenu();
        crmSubMenu.addItem("Legal Entities", e -> select(legalEntitiesView));
        crmSubMenu.addItem("Natural Entities", e -> select(naturalEntitiesView));
        crmSubMenu.addItem("Contracts", e -> select(contractsView));
        crmSubMenu.addItem("Employees", e -> select(employeesView));
        crmSubMenu.addItem("Interactions", e -> select(interactionsView));
        crmSubMenu.addItem("Activities", e -> select(activitiesView));
        crmSubMenu.addItem("Administration", e -> select(crmDomainView));

        MenuItem activities = menuBar.addItem("Products");
        SubMenu activitiesSubMenu = activities.getSubMenu();
        activitiesSubMenu.addItem("Products", e -> select(productsView));
        activitiesSubMenu.addItem("Assignments", e -> select(assignmentsView));
        activitiesSubMenu.addItem("Efforts", e -> select(effortsView));
        activitiesSubMenu.addItem("Administration", e -> select(productsDomainView));

        MenuItem invoices = menuBar.addItem("Invoices");
        SubMenu invoicesSubMenu = invoices.getSubMenu();
        invoicesSubMenu.addItem("Articles", e -> select(articlesView));
        invoicesSubMenu.addItem("Invoices", e -> select(invoicesView));
        invoicesSubMenu.addItem("Administration", e -> select(invoicesDomainView));

        MenuItem ledger = menuBar.addItem("Financials");
        SubMenu ledgerSubMenu = ledger.getSubMenu();
        ledgerSubMenu.addItem("Accounts", e -> select(accountsView));
        ledgerSubMenu.addItem("Transactions", e -> select(transactionsView));
        ledgerSubMenu.addItem("Analytics", e -> select(analyticsCrmView));
        ledgerSubMenu.addItem("Administration", e -> select(ledgerDomainView));

        MenuItem admin = menuBar.addItem("Admin");
        SubMenu adminSubmenu = admin.getSubMenu();
        adminSubmenu.addItem("Users", e -> select(subjectsView));
        adminSubmenu.addItem("Logout", e -> logout());

        return menuBar;
    }

    private void select(@NotNull Component view) {
        setContent(view);
        currentView = view;
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
