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

package net.tangly.commons.ledger.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.bus.ledger.LedgerBoundedDomain;
import net.tangly.commons.domain.ui.DomainView;
import net.tangly.commons.ui.BoundedDomainUi;
import net.tangly.commons.ui.MainLayout;
import net.tangly.commons.vaadin.Crud;
import org.jetbrains.annotations.NotNull;

public class LedgerBoundedDomainUi implements BoundedDomainUi {
    private final AccountsView accountsView;
    private final TransactionsView transactionsView;
    private final DomainView domainView;
    private Component currentView;

    @Inject
    public LedgerBoundedDomainUi(@NotNull LedgerBoundedDomain domain) {
        accountsView = new AccountsView(domain, Crud.Mode.EDITABLE);
        transactionsView = new TransactionsView(domain, Crud.Mode.EDITABLE);
        domainView = new DomainView(domain);
        currentView = transactionsView;
    }

    @Override
    public String name() {
        return "Ledger";
    }

    @Override
    public void select(@NotNull MainLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Accounts", e -> select(layout, accountsView));
        subMenu.addItem("Transactions", e -> select(layout, transactionsView));

        menuItem = menuBar.addItem(ADMINISTRATION);
        subMenu = menuItem.getSubMenu();
        subMenu.addItem(STATISTICS, e -> select(layout, domainView));
        select(layout, currentView);
    }

    private void select(@NotNull MainLayout layout, @NotNull Component view) {
        layout.setContent(view);
        currentView = view;
    }
}
