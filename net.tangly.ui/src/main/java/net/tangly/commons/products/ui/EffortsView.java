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

package net.tangly.commons.products.ui;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.products.Effort;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

class EffortsView extends EntitiesView<Effort> {
    private final ProductsBoundedDomain domain;
    private Binder<Effort> binder;

    public EffortsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        super(Effort.class, mode, domain.realm().efforts());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        Grid<Effort> grid = grid();
        grid.addColumn(Effort::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.assignment().id()).setKey("assignment").setHeader("Assignment").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.assignment().name()).setKey("collaborator").setHeader("Collaborator").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::duration).setKey("duration").setHeader("Duration").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::contractId).setKey("contractId").setHeader("ContractId").setAutoWidth(true).setResizable(true).setSortable(true);
        addAndExpand(grid(), gridButtons());
    }

    @Override
    protected Effort updateOrCreate(Effort entity) {
        return EntitiesView.updateOrCreate(entity, binder, Effort::new);
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Effort entity, FormLayout form) {
        TextField oid = new TextField("Oid", "oid");
        TextField assignment = VaadinUtils.createTextField("Assignment", "assignment", true, false);
        TextField collaborator = VaadinUtils.createTextField("Collaborator", "collaborator", true, false);
        TextField collaboratorId = VaadinUtils.createTextField("Collaborator ID", "collaborator id", true, false);
        DatePicker date = VaadinUtils.createDatePicker("Date");
        IntegerField duration = new IntegerField("Duration", "duration");
        TextField contractId = new TextField("Contract Id", "contractId");
        TextArea text = new TextArea("Text", "text");

        VaadinUtils.configureOid(operation, oid);
        VaadinUtils.readOnly(operation, date, duration, contractId, text);

        form.add(oid, assignment, collaborator, collaboratorId, contractId, date, duration);
        form.add(text, 3);

        binder = new Binder<>();
        binder.bind(oid, o -> Long.toString(o.oid()), null);
        binder.bind(assignment, o -> o.assignment().id(), null);
        binder.bind(collaborator, o -> o.assignment().name(), null);
        binder.bind(collaboratorId, o -> o.assignment().collaboratorId(), null);
        binder.bind(contractId, Effort::contractId, Effort::contractId);
        binder.bind(date, Effort::date, Effort::date);
        binder.bind(duration, Effort::duration, Effort::duration);
        binder.bind(text, Effort::text, Effort::text);
        if (entity != null) {
            binder.readBean(entity);
        }
        return form;
    }
}
