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

package net.tangly.commons.crm.products.ui;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.products.Effort;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class EffortsView extends EntitiesView<Effort> {
    private final ProductsBusinessLogic productsLogic;
    private Binder<Effort> binder;

    public EffortsView(@NotNull ProductsBusinessLogic productsLogic, @NotNull Mode mode) {
        super(Effort.class, mode, productsLogic.realm().efforts());
        this.productsLogic = productsLogic;
        initialize();
        addAndExpand(grid(), gridButtons());
    }

    @Override
    protected void initialize() {
        Grid<Effort> grid = grid();
        grid.addColumn(Effort::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::duration).setKey("duration").setHeader("Duration").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::contractId).setKey("contractId").setHeader("ContractId").setAutoWidth(true).setResizable(true).setSortable(true);
        // TODO add assignment name to identify collaborator
    }

    @Override
    protected Effort updateOrCreate(Effort entity) {
        return EntitiesView.updateOrCreate(entity, binder, Effort::new);
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Effort entity, FormLayout form) {
        TextField oid = new TextField("Oid", "oid");
        DatePicker date = new DatePicker("Date");
        IntegerField duration = new IntegerField("Duration", "duration");
        TextField contractId = new TextField("Contract Id", "contractId");
        TextArea text = new TextArea("Text", "text");

        VaadinUtils.configureOid(operation, oid);
        VaadinUtils.readOnly(operation, date, duration, contractId, text);

        form.add(oid, date, duration, contractId);
        form.add(text, 3);

        binder = new Binder<>();
        binder.bind(oid, o -> Long.toString(o.oid()), null);
        binder.bind(date, Effort::date, Effort::date);
        binder.bind(duration, Effort::duration, Effort::duration);
        binder.bind(contractId, Effort::contractId, Effort::contractId);
        binder.bind(text, Effort::text, Effort::text);
        if (entity != null) {
            binder.readBean(entity);
        }
        return form;
    }
}
