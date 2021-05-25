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

package net.tangly.erp.ledger.ui;

import java.util.Objects;
import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Crud;
import org.jetbrains.annotations.NotNull;

public class LedgerBoundedDomainUi implements BoundedDomainUi {
    private final LedgerBoundedDomain domain;
    private final AccountsView accountsView;
    private final TransactionsView transactionsView;
    private final AnalyticsLedgerView analyticsView;
    private final DomainView domainView;
    private Component currentView;

    @Inject
    public LedgerBoundedDomainUi(@NotNull LedgerBoundedDomain domain) {
        this.domain = domain;
        accountsView = new AccountsView(domain, Crud.Mode.EDITABLE);
        transactionsView = new TransactionsView(domain, Crud.Mode.EDITABLE);
        analyticsView = new AnalyticsLedgerView(domain);
        domainView = new DomainView(domain);
        currentView = transactionsView;
    }

    @Override
    public String name() {
        return "Ledger";
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Accounts", e -> select(layout, accountsView));
        subMenu.addItem("Transactions", e -> select(layout, transactionsView));

        addAnalytics(menuBar, layout, analyticsView);
        addAdministration(menuBar, layout, domain, domainView);
        select(layout, currentView);
    }

    @Override
    public void select(@NotNull AppLayout layout, Component view) {
        currentView = Objects.isNull(view) ? currentView : view;
        layout.setContent(currentView);
    }
}
