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

package net.tangly.erp.invoices.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.components.VaadinUtils;
import net.tangly.ui.grids.GridDecorators;
import org.jetbrains.annotations.NotNull;

@PageTitle("invoices-invoices")
class InvoicesView extends EntitiesView<Invoice> {
    private final InvoicesBoundedDomain domain;
    private final TextField id;
    private final TextField name;
    private final DatePicker invoicedDate;
    private final DatePicker dueDate;
    private final TextArea text;

    public InvoicesView(@NotNull InvoicesBoundedDomain domain, @NotNull Mode mode) {
        super(Invoice.class, mode, domain.realm().invoices());
        this.domain = domain;
        id = VaadinUtils.createTextField("Id", "id");
        name = VaadinUtils.createTextField("Name", "name");
        invoicedDate = new DatePicker("Invoiced Date");
        dueDate = new DatePicker("DUe Date");
        text = new TextArea("Text", "text");
        text.setWidthFull();
        initialize();
    }

    protected void initialize() {
        var grid = grid();
        grid.addColumn(Invoice::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::date).setKey("invoicedDate").setHeader("Invoiced Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::dueDate).setKey("dueDate").setHeader("Due Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> VaadinUtils.format(o.amountWithoutVat())).setKey("amountWithoutVat").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        GridDecorators<Invoice> decorators = gridFiltersAndActions(true, false);
        decorators.addFilter(new GridDecorators.FilterText<>(decorators, Invoice::id, "Id", "id"))
            .addFilter(new GridDecorators.FilterText<>(decorators, Invoice::name, "Name", "name"))
            .addFilter(new GridDecorators.FilterDate<>(decorators));
        decorators.addItemAction("Print", e -> new CmdCreateInvoiceDocument(selectedItem(), domain).execute());
        addAndExpand(decorators, grid(), gridButtons());
    }

    @Override
    protected Invoice updateOrCreate(Invoice entity) {
        Invoice invoice = (entity != null) ? entity : new Invoice();
        invoice.id(id.getValue());
        invoice.name(name.getValue());
        invoice.text(text.getValue());
        return invoice;
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Invoice entity, @NotNull FormLayout form) {
        VaadinUtils.configureId(operation, id);
        VaadinUtils.readOnly(operation, name, invoicedDate, dueDate, text);

        if (entity != null) {
            id.setValue(entity.id());
            name.setValue(entity.name());
            invoicedDate.setValue(entity.date());
            dueDate.setValue(entity.dueDate());
            text.setValue(entity.text());
        }

        form.add(id, name, invoicedDate, dueDate);
        form.add(new HtmlComponent("br"));
        form.add(text, 3);
        return form;
    }
}
