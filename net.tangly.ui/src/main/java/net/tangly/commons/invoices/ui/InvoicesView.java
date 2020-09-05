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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.vaadin.CrudActionsListener;
import net.tangly.commons.vaadin.CrudForm;
import net.tangly.commons.vaadin.ExternalEntitiesView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class InvoicesView extends ExternalEntitiesView<Invoice> {
    private final TextField id;
    private final TextField name;
    private final TextArea text;

    public InvoicesView(@NotNull Provider<Invoice> provider, @NotNull Mode mode) {
        super(Invoice.class, mode, provider);
        id = VaadinUtils.createTextField("Id", "id");
        name = VaadinUtils.createTextField("Name", "name");
        text = new TextArea("Text", "text");
        text.setWidthFull();
    }

    protected void initialize(@NotNull CrudForm<Invoice> form, @NotNull CrudActionsListener<Invoice> actionsListener) {
        super.initialize();
        Grid<Invoice> grid = grid();
        grid.addColumn(Invoice::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Invoice::dueDate).setKey("dueDate").setHeader("Due Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> VaadinUtils.format(o.amountWithoutVat())).setKey("amountWithoutVat").setHeader("Amount").setAutoWidth(true).setResizable(true)
                .setSortable(true);
    }

    @Override
    protected Invoice create() {
        Invoice invoice = new Invoice();
        invoice.id(id.getValue());
        invoice.name(name.getValue());
        invoice.text(text.getValue());
        return invoice;
    }

    @Override
    protected FormLayout prefillFrom(@NotNull Operation operation, Invoice entity, @NotNull FormLayout form) {
        boolean readonly = Operation.isReadOnly(operation);
        id.setReadOnly(readonly);
        name.setReadOnly(readonly);
        text.setReadOnly(readonly);

        id.setValue(entity.id());
        name.setValue(entity.name());
        text.setValue(entity.text());
        form.add(id, name);
        form.add(new HtmlComponent("br"));
        form.add(text, 2);
        return form;
    }
}
