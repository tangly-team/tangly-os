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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Command to create a document for an invoice. The command opens a dialog to select the options for the document creation.
 * If an invoice is selected, the dialog displays the generation options and removes the time range selection fields.
 * Otherwise, the dialog allows the selection of a time interval to select the documents to be generated and removes the file name field.
 */
public class CmdCreateInvoiceDocument implements Cmd {
    private final DatePicker from;
    private final DatePicker to;
    private final Checkbox withQrCode;
    private final Checkbox withEN16931;
    private final Checkbox overwrite;
    private final TextField name;
    private final transient InvoicesBoundedDomain domain;
    private transient Invoice invoice;
    private Dialog dialog;

    /**
     * Create a command to create a document for a specific invoice.
     *
     * @param invoice invoice for which the document is created
     * @param domain  bounded domain of the invoices
     */
    public CmdCreateInvoiceDocument(Invoice invoice, @NotNull InvoicesBoundedDomain domain) {
        this.domain = domain;
        this.invoice = null;
        name = new TextField("Name");
        from = new DatePicker("From");
        to = new DatePicker("To");
        name.setReadOnly(true);
        withQrCode = new Checkbox("with QR Code");
        withEN16931 = new Checkbox("with EN 16931");
        overwrite = new Checkbox("Overwrite Existing Document(s)");
        this.invoice = invoice;
        if (Objects.nonNull(invoice)) {
            name.setValue(invoice.name());
        }
    }

    @Override
    public void execute() {
        dialog = Cmd.createDialog("40em", create());
        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            if (invoice == null) {
                domain.port().exportInvoiceDocuments(withQrCode.getValue(), withEN16931.getValue(), overwrite.getValue(), from.getValue(), to.getValue());
            } else {
                domain.port().exportInvoiceDocument(invoice, withQrCode.getValue(), withEN16931.getValue(), overwrite.getValue());
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
        form.add(name, new HtmlComponent("br"), from, to, new HtmlComponent("br"), withQrCode, withEN16931, overwrite);
        if (invoice == null) {
            name.setVisible(false);
        } else {
            from.setVisible(false);
            to.setVisible(false);
        }
        return form;
    }
}