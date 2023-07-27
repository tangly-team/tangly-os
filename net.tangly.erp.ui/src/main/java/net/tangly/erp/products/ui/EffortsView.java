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

package net.tangly.erp.products.ui;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.asciidoc.AsciiDocField;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

import static net.tangly.ui.components.EntityView.OID;
import static net.tangly.ui.components.EntityView.OID_LABEL;

/**
 * Regular CRUD view on efforts abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("products-efforts")
class EffortsView extends ItemView<Effort> {
    static class EffortFilter extends ItemView.ItemFilter<Effort> {
        private LocalDate date;
        private String contractId;
        private String assignmentId;
        private String collaboratorId;
        private String text;

        public EffortFilter() {
        }

        public void date(LocalDate date) {
            this.date = date;
            dataView().refreshAll();
        }

        public void contractId(String contractId) {
            this.contractId = contractId;
            dataView().refreshAll();
        }

        public void assignmentId(String assignmentId) {
            this.assignmentId = assignmentId;
            dataView().refreshAll();
        }

        public void collaboratorId(String collaboratorId) {
            this.collaboratorId = collaboratorId;
        }

        public void text(String text) {
            this.text = text;
            dataView().refreshAll();
        }

        public boolean test(@NotNull Effort entity) {
            return matches(entity.contractId(), contractId) && matches(entity.assignment().id(), assignmentId) && matches(entity.assignment().collaboratorId(), collaboratorId) &&
                matches(entity.text(), text);
        }
    }

    static class EffortForm extends ItemForm<Effort, EffortsView> {

        EffortForm(@NotNull EffortsView parent) {
            super(parent);
            init();
        }

        @Override
        protected void init() {
            TextField oid = new TextField("Oid", "oid");
            TextField assignment = VaadinUtils.createTextField("Assignment", "assignment", true, false);
            TextField collaborator = VaadinUtils.createTextField("Collaborator", "collaborator", true, false);
            TextField collaboratorId = VaadinUtils.createTextField("Collaborator ID", "collaborator id", true, false);
            DatePicker date = VaadinUtils.createDatePicker("Date");
            IntegerField duration = new IntegerField("Duration", "duration");
            TextField contractId = new TextField("Contract Id", "contractId");
            AsciiDocField text = new AsciiDocField("Text");

            FormLayout form = new FormLayout();
            form.add(oid, assignment, collaborator, collaboratorId, contractId, date, duration);
            form.add(text, 3);

            binder().bind(oid, o -> Long.toString(o.oid()), null);
            binder().bind(assignment, o -> o.assignment().id(), null);
            binder().bind(collaborator, o -> o.assignment().name(), null);
            binder().bind(collaboratorId, o -> o.assignment().collaboratorId(), null);
            binder().bind(contractId, Effort::contractId, Effort::contractId);
            binder().bind(date, Effort::date, Effort::date);
            binder().bind(duration, Effort::duration, Effort::duration);
            binder().bind(text, Effort::text, Effort::text);
        }

        @Override
        public void clear() {

        }

        @Override
        protected Effort createOrUpdateInstance(Effort entity) throws ValidationException {
            return null;
        }
    }

    private final transient ProductsBoundedDomain domain;

    public EffortsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        super(Effort.class, domain, domain.realm().efforts(), new EffortFilter(), mode);
        this.domain = domain;
        init();
    }

    @Override
    protected void init() {
        var grid = grid();
        grid.addColumn(Effort::oid).setKey(OID).setHeader(OID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.assignment().id()).setKey("assignment").setHeader("Assignment").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.assignment().name()).setKey("collaborator").setHeader("Collaborator").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::duration).setKey("duration").setHeader("Duration").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::contractId).setKey("contractId").setHeader("ContractId").setAutoWidth(true).setResizable(true).setSortable(true);

    }
}
