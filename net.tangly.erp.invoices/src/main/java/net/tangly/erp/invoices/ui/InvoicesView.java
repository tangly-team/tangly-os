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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceItem;
import net.tangly.erp.invoices.domain.InvoiceLine;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

@PageTitle("invoices-invoices")
class InvoicesView extends ItemView<Invoice> {
    static class InvoiceFilter extends ItemFilter<Invoice> {
        @Override
        public boolean test(@NotNull Invoice entity) {
            return true;
        }
    }

    static class InvoiceForm extends ItemForm<Invoice, InvoicesView> {
        private final Grid<InvoiceLine> invoiceLines;

        InvoiceForm(@NotNull InvoicesView view) {
            super(view);
            invoiceLines = invoiceLines();
            addTabAt("details", details(), 0);
            addTabAt("lines", invoiceLines(), 1);
        }

        @Override
        public void value(Invoice value) {
            super.value(value);
            invoiceLines.setItems(DataProvider.ofCollection(Objects.nonNull(value) ? value.lines() : Collections.emptyList()));
        }

        private FormLayout details() {
            var id = new TextField(ID_LABEL, ID);
            var name = new TextField(NAME_LABEL, NAME);
            var invoicedDate = new DatePicker("Invoiced Date");
            var dueDate = new DatePicker("Due Date");
            AsciiDocField text = new AsciiDocField(EntityView.TEXT_LABEL);
            text.setWidthFull();

            var form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(id, name, invoicedDate, dueDate);
            form.add(new HtmlComponent("br"));
            form.add(text, 3);
            binder().bind(id, Invoice::id, Invoice::id);
            binder().bind(name, Invoice::name, Invoice::name);
            binder().bind(invoicedDate, Invoice::date, Invoice::date);
            binder().bind(dueDate, Invoice::dueDate, Invoice::dueDate);
            binder().bind(text, Invoice::text, Invoice::text);
            return form;
        }

        private Grid<InvoiceLine> invoiceLines() {
            var grid = new Grid<InvoiceLine>();
            grid.addColumn(InvoiceLine::position).setKey("position").setHeader("Position").setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(InvoiceLine::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(o -> VaadinUtils.format(o.amount())).setKey(AMOUNT).setHeader(AMOUNT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(o -> VaadinUtils.format(o.quantity())).setKey("quantity").setHeader("Quantity").setAutoWidth(true).setResizable(true)
                .setSortable(true);
            grid.addColumn(o -> VaadinUtils.format(o.unitPrice())).setKey("unitPrice").setHeader("Unit Price").setAutoWidth(true).setResizable(true)
                .setSortable(true);
            grid.addColumn(o -> VaadinUtils.format(o.vatRate())).setKey("vatRate").setHeader("VAT Rate").setAutoWidth(true).setResizable(true)
                .setSortable(true);
            grid.addColumn(o -> o.isItem() ? ((InvoiceItem) o).article().toString() : "").setKey("article").setHeader("Article").setAutoWidth(true)
                .setResizable(true).setSortable(true);
            return grid;
        }


        @Override
        protected Invoice createOrUpdateInstance(Invoice entity) throws ValidationException {
            Invoice invoice = (entity != null) ? entity : new Invoice();
            binder().writeBean(invoice);
            return invoice;
        }
    }

    public InvoicesView(@NotNull InvoicesBoundedDomainUi domain, @NotNull Mode mode) {
        super(Invoice.class, domain, domain.domain().realm().invoices(), new InvoiceFilter(), mode);
        form(() -> new InvoiceForm(this));
        init();
    }

    @Override
    public InvoicesBoundedDomainUi domainUi() {
        return (InvoicesBoundedDomainUi) super.domainUi();
    }

    @Override
    protected void addActions(@NotNull GridMenu<Invoice> menu) {
        menu().add(new Hr());
        menu().add(GridMenu.PRINT_TEXT, e -> Cmd.ofDualCmd(e, (Invoice o) -> new CmdCreateInvoiceDocument(o, domainUi()).execute()),
            GridMenu.MenuItemType.DUAL);
    }

    private void init() {
        var grid = grid();
        grid.addColumn(Invoice::id).setKey(ID).setHeader(ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::name).setKey(NAME).setHeader(NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::date).setKey("invoicedDate").setHeader("Invoiced Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::dueDate).setKey("dueDate").setHeader("Due Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new NumberRenderer<>(Invoice::amountWithoutVat, VaadinUtils.FORMAT)).setKey("amountWithoutVat").setHeader("Amount Without VAT")
            .setComparator(Invoice::amountWithoutVat).setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Invoice::text).setKey(TEXT).setHeader(TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
    }
}
