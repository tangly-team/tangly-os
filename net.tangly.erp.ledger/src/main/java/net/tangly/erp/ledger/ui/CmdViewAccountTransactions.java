/*
 * Copyright 2024 Marcel Baumann
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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.BigDecimalField;
import net.tangly.core.HasDate;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

/**
 * Displays all the transactions of an account in a dialog. The date range can be selected. The initial balance is displayed.
 * The relevant transactions are displayed in a grid.
 */
public class CmdViewAccountTransactions implements Cmd {
    private final LedgerBoundedDomain domain;
    private final Account account;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final BigDecimalField initialBalance;
    private Grid<AccountEntry> entries;
    private Dialog dialog;

    public CmdViewAccountTransactions(@NotNull LedgerBoundedDomain domain, @NotNull Account account) {
        this.domain = domain;
        this.account = account;
        fromDate = VaadinUtils.createDatePicker("From");
        fromDate.setValue(LocalDate.of(Year.now().getValue(), Month.JANUARY, 1));
        fromDate.setRequired(true);
        toDate = VaadinUtils.createDatePicker("To");
        toDate.setValue(LocalDate.of(Year.now().getValue(), Month.DECEMBER, 31));
        toDate.setRequired(true);
        initialBalance = new BigDecimalField(" Initial Balance");
        initialBalance.setReadOnly(true);
        entries = new Grid<>();
        entries.addColumn(AccountEntry::date).setKey(ItemView.DATE).setHeader(ItemView.DATE_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        entries.addColumn(o -> referenceFor(o)).setKey("reference").setHeader("Reference").setAutoWidth(true).setResizable(true).setSortable(true);
        entries.addColumn(o -> textFor(o)).setKey(ItemView.TEXT).setHeader(ItemView.TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        entries.addColumn(o -> accountIdFor(o)).setKey("account-id").setHeader("Account Id").setAutoWidth(true).setResizable(true).setSortable(true);
        entries.addColumn(o -> o.isDebit() ? o.amount() : null).setKey("debit").setHeader("Debit").setAutoWidth(true).setResizable(true).setSortable(true);
        entries.addColumn(o -> o.isCredit() ? o.amount() : null).setKey("credit").setHeader("Credit").setAutoWidth(true).setResizable(true).setSortable(true);
        entries.addColumn(o -> account.balance(o.date())).setKey("balance").setHeader("Balance").setAutoWidth(true).setResizable(true).setSortable(true);
        entries.setWidthFull();
        entries.setItems(entries(fromDate.getValue(), toDate.getValue()));
        initialBalance.setValue(account.balance(fromDate.getValue()));
        fromDate.addValueChangeListener(e -> {
            entries.setItems(entries(fromDate.getValue(), toDate.getValue()));
            initialBalance.setValue(account.balance(fromDate.getValue()));
        });
        toDate.addValueChangeListener(e -> {
            entries.setItems(entries(fromDate.getValue(), toDate.getValue()));
        });
    }

    @Override
    public void execute() {
        dialog = Cmd.createDialog("40em", create());
        Button close = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(close);
        dialog.open();
    }

    @Override
    public Dialog dialog() {
        return dialog;
    }

    protected void close() {
        dialog.close();
        dialog = null;
    }

    private FormLayout create() {
        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        form.add(fromDate, toDate, initialBalance, new HtmlComponent("br"), entries);
        form.setColspan(entries, 3);
        return form;
    }

    private String referenceFor(AccountEntry entry) {
        return domain.realm().transactions().items().stream().filter(o -> o.creditSplits().contains(entry) || o.debitSplits().contains(entry)).findAny()
            .orElseThrow().reference();
    }

    private String textFor(AccountEntry entry) {
        return domain.realm().transactions().items().stream().filter(o -> o.creditSplits().contains(entry) || o.debitSplits().contains(entry)).findAny()
            .orElseThrow().text();
    }

    private String accountIdFor(AccountEntry entry) {
        var transaction =
            domain.realm().transactions().items().stream().filter(o -> o.creditSplits().contains(entry) || o.debitSplits().contains(entry)).findAny()
                .orElseThrow();
        if (transaction.creditSplits().contains(entry) && transaction.debitSplits().size() == 1) {
            return transaction.debitSplits().getFirst().accountId();
        } else if (transaction.debitSplits().contains(entry) && transaction.creditSplits().size() == 1) {
            return transaction.creditSplits().getFirst().accountId();
        } else {
            return "*";
        }
    }

    private List<AccountEntry> entries(LocalDate from, LocalDate to) {
        return account.entries().stream().filter(new HasDate.IntervalFilter(from, to)).toList();
    }
}
