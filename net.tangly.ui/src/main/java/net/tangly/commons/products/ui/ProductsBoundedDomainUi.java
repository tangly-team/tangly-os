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

package net.tangly.commons.products.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.commons.domain.ui.CmdExportEntities;
import net.tangly.commons.domain.ui.CmdImportEntities;
import net.tangly.commons.domain.ui.DomainView;
import net.tangly.commons.ui.BoundedDomainUi;
import net.tangly.commons.ui.MainLayout;
import net.tangly.commons.vaadin.Crud;
import org.jetbrains.annotations.NotNull;

public class ProductsBoundedDomainUi implements BoundedDomainUi {
    private final ProductsBoundedDomain domain;
    private final ProductsView productsView;
    private final AssignmentsView assignmentsView;
    private final EffortsView effortsView;
    private final DomainView domainView;
    private Component currentView;

    @Inject
    public ProductsBoundedDomainUi(@NotNull ProductsBoundedDomain domain) {
        this.domain = domain;
        productsView = new ProductsView(domain, Crud.Mode.EDITABLE);
        assignmentsView = new AssignmentsView(domain, Crud.Mode.EDITABLE);
        effortsView = new EffortsView(domain, Crud.Mode.EDIT_DELETE);
        domainView = new DomainView(domain);
        currentView = productsView;
    }

    @Override
    public String name() {
        return "Products";
    }

    @Override
    public void select(@NotNull MainLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Products", e -> select(layout, productsView));
        subMenu.addItem("Assignments", e -> select(layout, assignmentsView));
        subMenu.addItem("Efforts", e -> select(layout, effortsView));

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
