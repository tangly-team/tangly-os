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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.invoices.InvoicesBoundedDomain;
import net.tangly.bus.ledger.LedgerBoundedDomain;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.commons.crm.ui.AnalyticsCrmView;
import net.tangly.commons.crm.ui.CmdChangePassword;
import net.tangly.commons.crm.ui.CmdLogin;
import net.tangly.commons.crm.ui.CmdLogout;
import net.tangly.commons.crm.ui.CrmBoundedDomainUi;
import net.tangly.commons.invoices.ui.InvoicesBoundedDomainUi;
import net.tangly.commons.ledger.ui.LedgerBoundedDomainUi;
import net.tangly.commons.products.ui.ProductsBoundedDomainUi;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.erp.Erp;
import org.jetbrains.annotations.NotNull;

@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/override-overlay.css", themeFor = "vaadin-dialog-overlay")
@CssImport(value = "./styles/override-negative.css", themeFor = "vaadin-grid")
@JsModule("prefers-color-scheme.js")
@PageTitle("tangly ERP")
@Route("")
public class MainLayout extends AppLayout {
    private static final CrmBoundedDomain crmDomain;
    private static final LedgerBoundedDomain ledgerDomain;
    private static final InvoicesBoundedDomain invoicesDomain;
    private static final ProductsBoundedDomain productsDomain;

    private final AnalyticsCrmView analyticsCrmView;

    private final Map<String, BoundedDomainUi> domains;
    private MenuBar menuBar;

    static {
        crmDomain = Erp.ofCrmDomain();
        invoicesDomain = Erp.ofInvoicesDomain();
        productsDomain = Erp.ofProductsDomain();
        ledgerDomain = Erp.ofLedgerDomain();
    }

    public MainLayout() {
        domains = new HashMap<>();
        put(new CrmBoundedDomainUi(crmDomain, invoicesDomain));
        put(new ProductsBoundedDomainUi(productsDomain));
        put(new InvoicesBoundedDomainUi(invoicesDomain));
        put(new LedgerBoundedDomainUi(ledgerDomain));

        analyticsCrmView = new AnalyticsCrmView(crmDomain.logic(), invoicesDomain.logic(), ledgerDomain.logic());

        Image image;
        try {
            byte[] buffer = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tangly70x70.png")).readAllBytes();
            image = new Image(new StreamResource("tangly70x70.png", () -> new ByteArrayInputStream(buffer)), "tangly70x70.png");
            image.setHeight("44px");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        setPrimarySection(Section.NAVBAR);
        menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        addToNavbar(new DrawerToggle(), image, menuBar, menuBar());
        drawerMenu();
        domains.get("Customers").select(this, menuBar);
    }

    @Override
    protected void onAttach(@NotNull AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (Objects.isNull(VaadinUtils.getAttribute(this, "subject"))) {
            new CmdLogin(crmDomain).execute();
        }
    }

    private MenuBar menuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        MenuItem admin = menuBar.addItem("Admin");
        SubMenu adminSubmenu = admin.getSubMenu();
        adminSubmenu.addItem("Logout", e -> new CmdLogout().execute());
        adminSubmenu.addItem("Change Password ...", e -> new CmdChangePassword(crmDomain).execute());
        return menuBar;
    }

    private void drawerMenu() {
        Tabs tabs = new Tabs(new Tab("Customers"), new Tab("Products"), new Tab("Invoices"), new Tab("Ledger"));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);
        tabs.addSelectedChangeListener(event -> {
            BoundedDomainUi domainUi = domains.get(event.getSelectedTab().getLabel());
            if (domainUi != null) {
                menuBar.removeAll();
                domainUi.select(this, menuBar);
            }
        });
    }

    private void put(BoundedDomainUi domainUi) {
        domains.put(domainUi.name(), domainUi);
    }
}
