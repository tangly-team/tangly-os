/*
 * Copyright 2006-2022 Marcel Baumann
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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.components.VaadinUtils;
import net.tangly.ui.grids.GridDecorators;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * Regular CRUD view on transactions abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("ledger-transactions")
class TransactionsView extends EntitiesView<Transaction> {
    private final transient LedgerBoundedDomain domain;
    private LocalDate from;
    private LocalDate to;
    private Binder<Transaction> binder;

    public TransactionsView(@NotNull LedgerBoundedDomain domain, @NotNull Mode mode) {
        super(Transaction.class, mode, domain.realm().transactions());
        this.domain = domain;
        from = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        to = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        initialize();
    }

    @Override
    protected void initialize() {
        Grid<Transaction> grid = grid();
        grid.addColumn(Transaction::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::debitAccount).setKey("debit").setHeader("Debit").setAutoWidth(true).setResizable(true);
        grid.addColumn(Transaction::creditAccount).setKey("credit").setHeader("Credit").setAutoWidth(true).setResizable(true);
        grid.addColumn(new NumberRenderer<>(Transaction::amount, VaadinUtils.FORMAT)).setKey("amount").setHeader("Amount").setAutoWidth(true).setResizable(true)
            .setTextAlign(ColumnTextAlign.END);
        GridDecorators<Transaction> decorators = gridFiltersAndActions(true, false);
        decorators.addFilter(new GridDecorators.FilterText<>(decorators, Transaction::reference, "Reference", "reference"))
            .addFilter(new GridDecorators.FilterText<>(decorators, Transaction::debitAccount, "Debit", "debit"))
            .addFilter(new GridDecorators.FilterText<>(decorators, Transaction::creditAccount, "Credit", "credit"))
            .addFilter(new GridDecorators.FilterDate<>(decorators)).addFilter(new GridDecorators.FilterText<>(decorators, Transaction::text, "Text", "text"));
        addAndExpand(decorators, grid(), gridButtons());
    }

    void interval(@NotNull LocalDate from, @NotNull LocalDate to) {
        this.from = from;
        this.to = to;
        grid().getDataProvider().refreshAll();
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Transaction entity, FormLayout form) {
        DatePicker date = VaadinUtils.createDatePicker("date");
        TextField text = VaadinUtils.createTextField("Text", "text");
        TextField reference = VaadinUtils.createTextField("Reference", "reference");
        ComboBox<String> debitAccount = new ComboBox<>("Debit");
        debitAccount.setItems(domain.logic().bookableAccountIds());
        debitAccount.setClearButtonVisible(true);
        ComboBox<String> creditAccount = new ComboBox<>("Credit");
        creditAccount.setItems(domain.logic().bookableAccountIds());
        creditAccount.setClearButtonVisible(true);
        TextField amount = VaadinUtils.createTextField("Amount", "amount");
        VaadinUtils.readOnly(operation, date, text, reference, debitAccount, creditAccount, amount);

        binder = new Binder<>();
        binder.bind(date, Transaction::date, null);
        binder.bind(debitAccount, Transaction::debitAccount, null);
        binder.bind(creditAccount, Transaction::creditAccount, null);
        binder.bind(amount, o -> o.text().toString(), null);
        binder.bind(text, Transaction::text, null);
        binder.bind(reference, Transaction::reference, null);
        // TODO debit and credit including splits
        form.add(date, reference, text, debitAccount, creditAccount, amount);
        form.setColspan(text, 3);
        if (entity != null) {
            binder.readBean(entity);
        }
        return form;
    }

    @Override
    protected Transaction updateOrCreate(Transaction entity) {
        Transaction transaction;
        if (entity == null) {
            LocalDate date = (LocalDate) binder.getBinding("date").orElseThrow().getField().getValue();
            String debitAccount = (String) binder.getBinding("debitAccount").orElseThrow().getField().getValue();
            String creditAccount = (String) binder.getBinding("creditAccount").orElseThrow().getField().getValue();
            // handle BigDecimal amount
            String text = (String) binder.getBinding("text").orElseThrow().getField().getValue();
            String reference = (String) binder.getBinding("reference").orElseThrow().getField().getValue();
            transaction = new Transaction(date, debitAccount, creditAccount, null, text, reference);
        } else {
            transaction = entity;
        }
        return transaction;
    }
}
