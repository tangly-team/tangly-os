/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.products.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class ProductsBoundedDomainUi extends BoundedDomainUi<ProductsBoundedDomain> {
    private final ProductsView productsView;
    private final AssignmentsView assignmentsView;
    private final EffortsView effortsView;
    private final DomainView domainView;

    public ProductsBoundedDomainUi(@NotNull ProductsBoundedDomain domain) {
        super(domain);
        productsView = new ProductsView(domain, Mode.EDIT);
        assignmentsView = new AssignmentsView(domain, Mode.EDIT);
        effortsView = new EffortsView(domain, Mode.DELETE);
        domainView = new DomainView(domain);
        currentView(productsView);
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Products", e -> select(layout, productsView));
        subMenu.addItem("Assignments", e -> select(layout, assignmentsView));
        subMenu.addItem("Efforts", e -> select(layout, effortsView));

        addAdministration(layout, menuBar, domainView, new CmdFilesUploadProducts(domain()));
        select(layout);
    }

    @Override
    public void refreshViews() {
        productsView.refresh();
        assignmentsView.refresh();
        effortsView.refresh();
    }
}