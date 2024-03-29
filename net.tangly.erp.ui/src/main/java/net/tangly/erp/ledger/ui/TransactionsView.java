/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.ledger.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Regular CRUD view on transaction abstraction. The grid and edition dialog were optimized for usability.
 */
@PageTitle("ledger-transactions")
class TransactionsView extends ItemView<Transaction> {
    static class TransactionFilter extends ItemFilter<Transaction> {
        private LocalDate date;
        private String text;
        private String debit;
        private String credit;
        private BigDecimal amount;

        public TransactionFilter() {
        }

        public void date(LocalDate date) {
            this.date = date;
            dataView().refreshAll();
        }

        public void text(String text) {
            this.text = text;
            dataView().refreshAll();
        }

        public void debit(String debit) {
            this.debit = debit;
            dataView().refreshAll();
        }

        public void credit(String credit) {
            this.credit = credit;
            dataView().refreshAll();
        }

        public boolean test(@NotNull Transaction entity) {
            return (Objects.isNull(date) || (date.isEqual(entity.date()))) && matches(entity.text(), text) && matches(entity.debit().accountId(), debit) &&
                matches(entity.credit().accountId(), credit);
        }
    }

    static class TransactionForm extends ItemForm<Transaction, TransactionsView> {
        TransactionForm(@NotNull TransactionsView view) {
            super(view);
            init();
        }

        protected void init() {
            DatePicker date = VaadinUtils.createDatePicker("date");
            TextField text = VaadinUtils.createTextField("Text", "text");
            TextField reference = VaadinUtils.createTextField("Reference", "reference");
            ComboBox<String> debitAccount = new ComboBox<>("Debit");

            debitAccount.setItems(logic().bookableAccountIds());
            debitAccount.setClearButtonVisible(true);
            ComboBox<String> creditAccount = new ComboBox<>("Credit");
            creditAccount.setItems(logic().bookableAccountIds());
            creditAccount.setClearButtonVisible(true);
            TextField amount = VaadinUtils.createTextField("Amount", "amount");

            binder().bind(date, Transaction::date, null);
            binder().bind(debitAccount, Transaction::debitAccount, null);
            binder().bind(creditAccount, Transaction::creditAccount, null);
            binder().bind(amount, o -> o.text().toString(), null);
            binder().bind(text, Transaction::text, null);
            binder().bind(reference, Transaction::reference, null);
            // TODO debit and credit including splits
            FormLayout form = new FormLayout();
            form.add(date, reference, text, debitAccount, creditAccount, amount);
            addTabAt("details", form, 0);
        }

        @Override
        public void value(Transaction value) {
            if (value != null) {
                binder().readBean(value);
            }
        }

        @Override
        public void mode(@NotNull Mode mode) {
        }

        @Override
        public void clear() {
        }

        @Override
        protected Transaction createOrUpdateInstance(Transaction entity) throws ValidationException {
            Transaction transaction;
            if (entity == null) {
                LocalDate date = (LocalDate) binder().getBinding("date").orElseThrow().getField().getValue();
                String debitAccount = (String) binder().getBinding("debitAccount").orElseThrow().getField().getValue();
                String creditAccount = (String) binder().getBinding("creditAccount").orElseThrow().getField().getValue();
                // handle BigDecimal amount
                String text = (String) binder().getBinding("text").orElseThrow().getField().getValue();
                String reference = (String) binder().getBinding("reference").orElseThrow().getField().getValue();
                transaction = new Transaction(date, debitAccount, creditAccount, null, text, reference);
            } else {
                transaction = entity;
            }
            return transaction;
        }

        LedgerBusinessLogic logic() {
            return (LedgerBusinessLogic) parent().domain().logic();
        }
    }

    private final transient LedgerBoundedDomain domain;
    private Binder<Transaction> binder;

    public TransactionsView(@NotNull LedgerBoundedDomain domain, @NotNull Mode mode) {
        super(Transaction.class, domain, domain.realm().transactions(), null, mode);
        this.domain = domain;
        init();
    }

    private void init() {
        Grid<Transaction> grid = grid();
        grid.addColumn(Transaction::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::debitAccount).setKey("debit").setHeader("Debit").setAutoWidth(true).setResizable(true);
        grid.addColumn(Transaction::creditAccount).setKey("credit").setHeader("Credit").setAutoWidth(true).setResizable(true);
        grid.addColumn(new NumberRenderer<>(Transaction::amount, VaadinUtils.FORMAT)).setKey("amount").setHeader("Amount").setAutoWidth(true).setResizable(true)
            .setTextAlign(ColumnTextAlign.END);

        addEntityFilterFields(grid(), new TransactionFilter());
        buildMenu();
    }

    protected void addEntityFilterFields(@NotNull Grid<Transaction> grid, @NotNull TransactionFilter filter) {
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.getHeaderRows().clear();
        //addFilterText(headerRow, "date", "Date", filter::date);
        headerRow.getCell(grid.getColumnByKey(EntityView.TEXT)).setComponent(createTextFilterField(filter::text));
        headerRow.getCell(grid.getColumnByKey("debit")).setComponent(createTextFilterField(filter::debit));
        headerRow.getCell(grid.getColumnByKey("credit")).setComponent(createTextFilterField(filter::credit));
        // TODO amount
    }
}
