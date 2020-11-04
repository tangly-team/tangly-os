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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.bus.ledger.LedgerBusinessLogic;
import net.tangly.commons.vaadin.VaadinUtils;

public class CommandCreateLedgerDocument extends Dialog {
    private final TextField name;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final Checkbox withVat;
    private final Checkbox withTransactions;

    public CommandCreateLedgerDocument(LedgerBusinessLogic logic) {
        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);

        name = new TextField("Name", "document name");
        fromDate = VaadinUtils.createDatePicker("From");
        toDate = VaadinUtils.createDatePicker("To");
        withVat = new Checkbox("Include VAT Report");
        withTransactions = new Checkbox("Include Transactions");

        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            logic.port().exportLedgerDocument(name.getValue(), fromDate.getValue(), toDate.getValue(), withVat.getValue(), withTransactions.getValue());
            this.close();
        });
        Button cancel = new Button("Cancel", e -> this.close());

        form.add(name, new HtmlComponent("br"), fromDate, toDate, withVat, withTransactions, new HtmlComponent("br"), new HorizontalLayout(execute, cancel));
        add(form);
        open();
    }
}
