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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.ledger.Ledger;
import net.tangly.bus.ledger.Transaction;
import net.tangly.commons.vaadin.Crud;
import net.tangly.commons.vaadin.CrudForm;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class TransactionsView extends Crud<Transaction> implements CrudForm<Transaction> {
    public TransactionsView(@NotNull Ledger ledger, @NotNull Mode mode) {
        super(Transaction.class, mode, TransactionsView::defineTransactionsView, DataProvider.ofCollection(ledger.transactions()));
    }

    public static void defineTransactionsView(@NotNull Grid<Transaction> grid) {
        grid.addColumn(Transaction::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::debitAccount).setKey("debit").setHeader("Debit").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::creditAccount).setKey("credit").setHeader("Credit").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> VaadinUtils.format(o.amount())).setKey("amount").setHeader("Amount").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    public FormLayout createForm(@NotNull Operation operation, Transaction entity) {
        return null;
    }

    @Override
    public Transaction formCompleted(@NotNull Operation operation, Transaction entity) {
        return null;
    }
}
