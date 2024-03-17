/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.erp.Erp;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.ui.CmdChangePassword;
import net.tangly.erp.crm.ui.CmdLogin;
import net.tangly.erp.crm.ui.CmdLogout;
import net.tangly.erp.crm.ui.CrmBoundedDomainUi;
import net.tangly.erp.invoices.ui.InvoicesBoundedDomainUi;
import net.tangly.erp.ledger.ui.LedgerBoundedDomainUi;
import net.tangly.erp.products.ui.ProductsBoundedDomainUi;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@PageTitle("tangly ERP")
@Route("")
public class MainView extends AppLayout {
    private final Map<String, BoundedDomainUi> uiDomains;
    private final MenuBar menuBar;
    private final String defaultDomainName;


    public MainView() {
        defaultDomainName = Erp.instance().crmBoundedDomain().name();
        uiDomains = new HashMap<>();
        if (Erp.instance().crmBoundedDomain() != null) {
            put(new CrmBoundedDomainUi(Erp.instance().crmBoundedDomain(), Erp.instance().invoicesBoundedDomain()));
        }
        if (Erp.instance().productsBoundedDomain() != null) {
            put(new ProductsBoundedDomainUi(Erp.instance().productsBoundedDomain()));
        }
        if (Erp.instance().invoicesBoundedDomain() != null) {
            put(new InvoicesBoundedDomainUi(Erp.instance().invoicesBoundedDomain()));
        }
        if (Erp.instance().ledgerBoundedDomain() != null) {
            put(new LedgerBoundedDomainUi(Erp.instance().ledgerBoundedDomain()));
        }

        Image image;
        try {
            byte[] buffer = Thread.currentThread().getContextClassLoader().getResourceAsStream("tangly70x70.png").readAllBytes();
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
        select(defaultDomainName);
    }

    @Override
    protected void onAttach(@NotNull AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (Objects.isNull(VaadinUtils.getAttribute(this, "subject")) && Objects.nonNull(Erp.instance().crmBoundedDomain())) {
            new CmdLogin(Erp.instance().crmBoundedDomain()).execute();
        }
    }

    private MenuBar menuBar() {
        var menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        var admin = menuBar.addItem("Admin");
        var adminSubmenu = admin.getSubMenu();
        adminSubmenu.addItem("Logout", e -> new CmdLogout().execute());
        adminSubmenu.addItem("Change Password ...", e -> new CmdChangePassword(Erp.instance().crmBoundedDomain(), (Subject) VaadinUtils.getAttribute(this, "subject")).execute());
        adminSubmenu.addItem("Shutdown", e -> uiDomains.values().stream().map(BoundedDomainUi::domain).forEach(BoundedDomain::shutdown));
        return menuBar;
    }

    private void drawerMenu() {
        Tabs tabs = new Tabs(new Tab(tabNameFor(Erp.instance().crmBoundedDomain())), new Tab(tabNameFor(Erp.instance().productsBoundedDomain())),
            new Tab(tabNameFor(Erp.instance().invoicesBoundedDomain())), new Tab(tabNameFor(Erp.instance().ledgerBoundedDomain())));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);
        tabs.addSelectedChangeListener(event -> select(event.getSelectedTab().getLabel()));
    }

    private String tabNameFor(@NotNull BoundedDomain<?, ?, ?> domain) {
        return domain.name().substring(0, 1).toUpperCase() + domain.name().substring(1);
    }

    private void put(@NotNull BoundedDomainUi domainUi) {
        uiDomains.put(domainUi.name(), domainUi);
    }

    private void select(@NotNull String name) {
        var domainUi = uiDomains.get(name.toLowerCase());
        if (domainUi != null) {
            menuBar.removeAll();
            domainUi.select(this, menuBar);
        }
    }
}
