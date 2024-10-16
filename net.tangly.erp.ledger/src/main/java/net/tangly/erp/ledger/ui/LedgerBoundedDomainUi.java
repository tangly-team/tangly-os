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
import net.tangly.core.domain.Document;
import net.tangly.core.domain.DomainEntity;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DocumentsView;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.app.domain.UserManualView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class LedgerBoundedDomainUi extends BoundedDomainUi<LedgerBoundedDomain> {
    public static final String ACCOUNTS = "Accounts";
    public static final String TRANSACTIONS = "Transactions";

    public LedgerBoundedDomainUi(@NotNull LedgerBoundedDomain domain) {
        super(domain);
        addView(Account.class, new LazyReference<>(() -> new AccountsView(this, Mode.EDITABLE)));
        addView(Transaction.class, new LazyReference<>(() -> new TransactionsView(this, Mode.EDITABLE)));
        addView(Document.class, new LazyReference<>(() -> new DocumentsView(this, domain().realm().documents(), Mode.EDITABLE)));
        addView(DomainEntity.class, new LazyReference<>(() -> new DomainView(this)));
        addView(AnalyticsLedgerView.class, new LazyReference<>(() -> new AnalyticsLedgerView(this)));
        addView(UserManualView.class, new LazyReference<>(() -> new UserManualView(this)));
        currentView(view(Transaction.class).orElseThrow());
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(ACCOUNTS, _ -> select(layout, view(Account.class).orElseThrow()));
        subMenu.addItem(TRANSACTIONS, _ -> select(layout, view(Transaction.class).orElseThrow()));
        subMenu.addItem(DOCUMENTS, e -> select(layout, view(Document.class).orElseThrow()));

        menuItem = menuBar.addItem(TOOLS);
        subMenu = menuItem.getSubMenu();
        subMenu.addItem(ANALYTICS, _ -> select(layout, view(AnalyticsLedgerView.class).orElseThrow()));
        subMenu.addItem(USER_MANUAL, _ -> select(layout, view(UserManualView.class).orElseThrow()));
        addAdministration(layout, subMenu, view(DomainEntity.class).orElseThrow());
        select(layout);
    }
}
