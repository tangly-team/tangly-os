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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.bus.ledger.Account;
import net.tangly.bus.ledger.BusinessLogicLedger;
import net.tangly.bus.providers.RecordProviderInMemory;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.ledger.ports.LedgerPort;
import org.jetbrains.annotations.NotNull;

public class AccountsView extends EntitiesView<Account> {
    private TextField id;
    private LocalDate from;
    private LocalDate to;

    /**
     * Constructor of the CRUD view for accounts of the ledger.
     *
     * @param ledgerLogic ledger business lodgic which accounts should be displayed
     * @param mode        mode of the view
     */
    public AccountsView(@NotNull BusinessLogicLedger ledgerLogic, @NotNull Mode mode) {
        super(Account.class, mode, RecordProviderInMemory.of(ledgerLogic.ledger().accounts()));
        from = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        to = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        initializeGrid();
    }

    @Override
    protected void initializeGrid() {
        Grid<Account> grid = grid();
        grid.addColumn(Account::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::group).setKey("group").setHeader("Group").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> o.balance(from), VaadinUtils.FORMAT)).setKey("opening").setHeader("Opening").setAutoWidth(true)
                .setResizable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(VaadinUtils.coloredRender(o -> o.balance(to), VaadinUtils.FORMAT)).setKey("balance").setHeader("Balance").setAutoWidth(true)
                .setResizable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Account::kind).setKey("kind").setHeader("Kind").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::ownedBy).setKey("ownedBy").setHeader("Owned By").setAutoWidth(true).setResizable(true);
        addAndExpand(grid(), createCrudButtons());
    }

    public void interval(@NotNull LocalDate from, @NotNull LocalDate to) {
        this.from = from;
        this.to = to;
        grid().getDataProvider().refreshAll();
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Account entity, FormLayout form) {
        return form;
    }

    @Override
    protected Account updateOrCreate(Account entity) {
        return null;
    }
}
