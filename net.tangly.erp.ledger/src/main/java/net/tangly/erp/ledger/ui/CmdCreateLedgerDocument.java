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

public class CmdCreateLedgerDocument implements Cmd {
    private final TextField name;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final Checkbox withVat;
    private final Checkbox withTransactions;
    private final Checkbox yearlyReports;
    private final LedgerBoundedDomain domain;
    private Dialog dialog;

    public CmdCreateLedgerDocument(@NotNull LedgerBoundedDomain domain) {
        this.domain = domain;
        name = new TextField("Name", "document name");
        fromDate = VaadinUtils.createDatePicker("From");
        toDate = VaadinUtils.createDatePicker("To");
        withVat = new Checkbox("Include VAT Report");
        withTransactions = new Checkbox("Include Transactions");
        yearlyReports = new Checkbox("Yearly Reports");
    }

    @Override
    public void execute() {
        dialog = Cmd.createDialog("40em", create());
        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            if (yearlyReports.getValue()) {
                batchReports(name.getValue(), fromDate.getValue().getYear());
            } else {
                domain.port().exportLedgerDocument(name.getValue(), fromDate.getValue(), toDate.getValue(), withVat.getValue(), withTransactions.getValue());
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
        form.add(name, new HtmlComponent("br"), fromDate, toDate, withVat, withTransactions);
        return form;
    }

    private void batchReports(String name, int fromYear) {
        int currentYear = Year.now().getValue();
        for (int year = fromYear; year <= currentYear; year++) {
            domain.port().exportLedgerDocument(name + "-" + year, LocalDate.of(year, Month.JANUARY, 1), LocalDate.of(year, Month.DECEMBER, 31),
                withVat.getValue(),
                withTransactions.getValue());
        }
    }
}
