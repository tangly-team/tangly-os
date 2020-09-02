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
import java.util.function.Function;
import java.util.function.Predicate;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.bus.ledger.Transaction;
import net.tangly.commons.vaadin.Crud;
import net.tangly.commons.vaadin.CrudActionsListener;
import net.tangly.commons.vaadin.CrudForm;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.ledger.ports.LedgerBusinessLogic;
import org.jetbrains.annotations.NotNull;

public class TransactionsView extends Crud<Transaction> implements CrudForm<Transaction> {
    private LocalDate from;
    private LocalDate to;

    /**
     * Constructor of the CRUD view for accounts of the ledger.
     *
     * @param ledgerLogic ledger business lodgic which accounts should be displayed
     * @param mode        mode of the view
     */
    public TransactionsView(@NotNull LedgerBusinessLogic ledgerLogic, @NotNull Mode mode) {
        super(Transaction.class, mode, DataProvider.ofCollection(ledgerLogic.ledger().transactions()));
        from = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        to = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        initialize(this, null);
    }

    static class InTime<T> implements Predicate<T> {
        private LocalDate from;
        private LocalDate to;
        private final Function<T, LocalDate> date;

        InTime(Function<T, LocalDate> date) {
            this.date = date;
        }

        void interval(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean test(T object) {
            return ((from == null) || from.isAfter(date.apply(object))) && ((to == null) || to.isBefore(date.apply(object)));
        }
    }

    public static void defineTransactionsView(@NotNull Grid<Transaction> grid) {
    }

    protected void initialize(@NotNull CrudForm<Transaction> form, @NotNull CrudActionsListener<Transaction> actionsListener) {
        super.initialize(form, actionsListener);
        Grid<Transaction> grid = grid();
        grid.addColumn(Transaction::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::debitAccount).setKey("debit").setHeader("Debit").setAutoWidth(true).setResizable(true);
        grid.addColumn(Transaction::creditAccount).setKey("credit").setHeader("Credit").setAutoWidth(true).setResizable(true);
        grid.addColumn(new NumberRenderer<>(Transaction::amount, VaadinUtils.FORMAT)).setKey("amount").setHeader("Amount").setAutoWidth(true).setResizable(true)
                .setTextAlign(ColumnTextAlign.END);
    }

    void interval(@NotNull LocalDate from, @NotNull LocalDate to) {
        this.from = from;
        this.to = to;
        grid().getDataProvider().refreshAll();
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
