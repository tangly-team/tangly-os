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

package net.tangly.erp.ledger.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class LedgerBoundedDomainUi extends BoundedDomainUi<LedgerBoundedDomain> {
    public static final String ACCOUNTS = "Accounts";
    public static final String TRANSACTIONS = "Transactions";

    public LedgerBoundedDomainUi(@NotNull LedgerBoundedDomain domain) {
        super(domain);
        addView(ACCOUNTS, new LazyReference<>(() -> new AccountsView(this, Mode.EDITABLE)));
        addView(TRANSACTIONS, new LazyReference<>(() -> new TransactionsView(this, Mode.EDITABLE)));
        addView(ANALYTICS, new LazyReference<>(() -> new AnalyticsLedgerView(this)));
        addView(ENTITIES, new LazyReference<>(() -> new DomainView(this)));
        currentView(view(TRANSACTIONS).orElseThrow());
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(ACCOUNTS, _ -> select(layout, view(ACCOUNTS).orElseThrow()));
        subMenu.addItem(TRANSACTIONS, _ -> select(layout, view(TRANSACTIONS).orElseThrow()));

        addAnalytics(layout, menuBar, view(ANALYTICS).orElseThrow());
        addAdministration(layout, menuBar, view(ENTITIES).orElseThrow());
        select(layout);
    }
}
