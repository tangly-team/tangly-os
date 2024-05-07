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

package net.tangly.erp.invoices.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class CmdCreateInvoiceDocument implements Cmd {
    private final Checkbox withQrCode;
    private final Checkbox withEN16931;
    private final Checkbox overwrite;
    private final TextField name;
    private final transient InvoicesBoundedDomain domain;
    private final transient Invoice invoice;
    private Dialog dialog;

    public CmdCreateInvoiceDocument(@NotNull Invoice invoice, @NotNull InvoicesBoundedDomain domain) {
        this.invoice = invoice;
        this.domain = domain;
        name = new TextField("Name");
        name.setReadOnly(true);
        name.setValue(invoice.name());
        withQrCode = new Checkbox("with QR Code");
        withEN16931 = new Checkbox("with EN 16931");
        overwrite = new Checkbox("Overwrite existing documents");
    }

    @Override
    public void execute() {
        dialog = Cmd.createDialog("40em", create());
        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            domain.port().exportInvoiceDocument(invoice, withQrCode.getValue(), withEN16931.getValue(), overwrite.getValue());
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
        form.add(name, new HtmlComponent("br"), withQrCode, withEN16931, overwrite);
        return form;
    }
}
