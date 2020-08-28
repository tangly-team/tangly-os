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

package net.tangly.commons.ledger.ui;

import java.time.LocalDate;
import java.util.Collection;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import net.tangly.bus.ledger.Account;
import net.tangly.bus.ledger.Ledger;
import net.tangly.bus.providers.RecordProviderInMemory;
import net.tangly.commons.vaadin.ExternalEntitiesView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class AccountsView extends ExternalEntitiesView<Account> {
    /**
     * Constructor of the CRUD view for accounts of the ledger.
     *
     * @param ledger ledger which accounts should be displayed
     * @param mode mode of the view
     */
    public AccountsView(@NotNull Ledger ledger, @NotNull Mode mode) {
        super(Account.class, mode, AccountsView::defineAccountsView, RecordProviderInMemory.of((Collection)ledger.accounts()));
    }

    public static void defineAccountsView(@NotNull Grid<Account> grid) {
        ExternalEntitiesView.defineExternalEntitiesView(grid);
        grid.addColumn(Account::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Account::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> VaadinUtils.format(o.balance(LocalDate.now()))).setKey("balance").setHeader("Balance").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Account::kind).setKey("kind").setHeader("Kind").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Account::group).setKey("group").setHeader("Group").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Account::ownedBy).setKey("ownedBy").setHeader("Owned By").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected Account create() {
        return null;
    }

    @Override
    protected FormLayout prefillFrom(@NotNull Operation operation, Account entity, FormLayout form) {
        return null;
    }
}
