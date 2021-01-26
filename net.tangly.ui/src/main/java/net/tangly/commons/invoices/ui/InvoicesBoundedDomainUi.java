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

package net.tangly.commons.invoices.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.bus.invoices.InvoicesBoundedDomain;
import net.tangly.commons.domain.ui.CmdExportEntities;
import net.tangly.commons.domain.ui.CmdImportEntities;
import net.tangly.commons.domain.ui.DomainView;
import net.tangly.commons.ui.BoundedDomainUi;
import net.tangly.commons.ui.MainLayout;
import net.tangly.commons.vaadin.Crud;
import org.jetbrains.annotations.NotNull;

public class InvoicesBoundedDomainUi implements BoundedDomainUi {
    private final InvoicesBoundedDomain domain;
    private final ArticlesView articlesView;
    private final InvoicesView invoicesView;
    private final DomainView domainView;
    private Component currentView;

    @Inject
    public InvoicesBoundedDomainUi(@NotNull InvoicesBoundedDomain domain) {
        this.domain = domain;
        articlesView = new ArticlesView(domain, Crud.Mode.EDITABLE);
        invoicesView = new InvoicesView(domain, Crud.Mode.EDITABLE);
        domainView = new DomainView(domain);
        currentView = invoicesView;
    }

    @Override
    public String name() {
        return "Invoices";
    }

    @Override
    public void select(@NotNull MainLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Articles", e -> select(layout, articlesView));
        subMenu.addItem("Invoices", e -> select(layout, invoicesView));

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
