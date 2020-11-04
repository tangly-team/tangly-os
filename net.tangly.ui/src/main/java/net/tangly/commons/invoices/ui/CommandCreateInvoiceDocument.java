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

package net.tangly.commons.invoices.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoicesBusinessLogic;
import net.tangly.commons.vaadin.VaadinUtils;

public class CommandCreateInvoiceDocument extends Dialog {
    Checkbox withQrCode;
    Checkbox withEN16931;

    public CommandCreateInvoiceDocument(Invoice invoice, InvoicesBusinessLogic logicInvoices) {
        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);

        TextField name = new TextField("Name");
        name.setReadOnly(true);
        name.setValue(invoice.name());
        withQrCode = new Checkbox("with QR Code");
        withEN16931 = new Checkbox("with EN 16931");

        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            logicInvoices.port().exportInvoiceDocument(invoice, withQrCode.getValue(), withEN16931.getValue());
            this.close();
        });
        Button cancel = new Button("Cancel", e -> this.close());

        form.add(name, new HtmlComponent("br"), withQrCode, withEN16931, new HtmlComponent("br"), new HorizontalLayout(execute, cancel));
        add(form);
        open();
    }
}
