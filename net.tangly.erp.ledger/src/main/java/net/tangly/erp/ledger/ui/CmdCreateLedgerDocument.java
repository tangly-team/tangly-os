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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

/**
 * Command to create a ledger document for a given period. The command is parameterized with the bounded domain to access the ledger and the UI components to
 * capture the period and the options of the document.
 * <p>The from and to date are mandatory.</p>
 */
public class CmdCreateLedgerDocument implements Cmd {
    private final TextField name;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final Checkbox withBalanceSheet;
    private final Checkbox withProfitsAndLosses;
    private final Checkbox withEmptyAccounts;
    private final Checkbox withTransactions;
    private final Checkbox withVat;
    private final Checkbox yearlyReports;
    private final LedgerBoundedDomain domain;
    private Dialog dialog;

    public CmdCreateLedgerDocument(@NotNull LedgerBoundedDomain domain) {
        this.domain = domain;
        name = new TextField("Name", "document name");
        fromDate = VaadinUtils.createDatePicker("From");
        fromDate.setRequired(true);
        toDate = VaadinUtils.createDatePicker("To");
        toDate.setRequired(true);
        withBalanceSheet = new Checkbox("Include Balance Sheet");
        withBalanceSheet.setValue(true);
        withProfitsAndLosses = new Checkbox("Include Profits and Losses");
        withProfitsAndLosses.setValue(true);
        withEmptyAccounts = new Checkbox("Include Empty Accounts");
        withTransactions = new Checkbox("Include Transactions");
        withVat = new Checkbox("Include VAT Report");
        yearlyReports = new Checkbox("Yearly Reports");
    }

    @Override
    public void execute() {
        dialog = Cmd.createDialog("40em", create());
        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            if (yearlyReports.getValue()) {
                batchReports(name.getValue(), fromDate.getValue().getYear(), toDate.getValue().getYear());
            } else {
                domain.port()
                    .exportLedgerDocument(name.getValue(), fromDate.getValue(), toDate.getValue(), withBalanceSheet.getValue(), withProfitsAndLosses.getValue(),
                        withEmptyAccounts.getValue(), withTransactions.getValue(), withVat.getValue());
            }
            close();
        });
        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(execute, cancel);
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
        form.add(name, new HtmlComponent("br"), fromDate, toDate, withBalanceSheet, withProfitsAndLosses, withEmptyAccounts, withTransactions, withVat,
            new HtmlComponent("br"), yearlyReports);
        return form;
    }

    /**
     * Creates yearly rports for the given period.
     *
     * @param name     perfix of the report name. A dash and the year will be appended to the name.
     * @param fromYear first year of the reports
     * @param toYear   last year of the reports
     */
    private void batchReports(String name, int fromYear, int toYear) {
        int currentYear = Year.now().getValue();
        for (int year = fromYear; year <= toYear; year++) {
            domain.port().exportLedgerDocument("%s-%d".formatted(name, year), LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31),
                withBalanceSheet.getValue(), withProfitsAndLosses.getValue(), withEmptyAccounts.getValue(), withTransactions.getValue(), withVat.getValue());
        }
    }
}
