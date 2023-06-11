/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.invoices.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

@PageTitle("invoices-invoices")
class InvoicesView extends ItemView<Invoice> {
    static class InvoiceFilter extends ItemFilter<Invoice> {
        @Override
        public boolean test(@NotNull Invoice entity) {
            return true;
        }
    }

    static class InvoiceForm extends ItemForm<Invoice, InvoicesView> {
        InvoiceForm(@NotNull InvoicesView parent) {
            super(parent);
            init();
        }

        @Override
        protected void init() {
            TextField id = new TextField("Id", "id");
            TextField name = new TextField("Name", "name");
            DatePicker invoicedDate = new DatePicker("Invoiced Date");
            DatePicker dueDate = new DatePicker("DUe Date");
            TextArea text = new TextArea("Text", "text");
            text.setWidthFull();

            FormLayout form = new FormLayout();
            form.add(id, name, invoicedDate, dueDate);
            form.add(new HtmlComponent("br"));
            form.add(text, 3);
            binder().bind(id, Invoice::id, Invoice::id);
            binder().bind(name, Invoice::name, Invoice::name);
            binder().bind(invoicedDate, Invoice::date, Invoice::date);
            binder().bind(dueDate, Invoice::dueDate, Invoice::dueDate);
            binder().bind(text, Invoice::text, Invoice::text);
        }

        @Override
        public void clear() {

        }

        @Override
        protected Invoice createOrUpdateInstance(Invoice entity) throws ValidationException {
            Invoice invoice = (entity != null) ? entity : new Invoice();
            binder().writeBean(invoice);
            return invoice;
        }
    }

    public InvoicesView(@NotNull InvoicesBoundedDomain domain, @NotNull Mode mode) {
        super(Invoice.class, domain, domain.realm().invoices(), new InvoiceFilter(), mode);
        init();
    }

    protected void init() {
        var grid = grid();
        grid.addColumn(Invoice::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::date).setKey("invoicedDate").setHeader("Invoiced Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::dueDate).setKey("dueDate").setHeader("Due Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> VaadinUtils.format(o.amountWithoutVat())).setKey("amountWithoutVat").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        //        decorators.addItemAction("Print", e -> new CmdCreateInvoiceDocument(value(), domain).execute());
    }
}
