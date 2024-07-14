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

package net.tangly.erp.products.ui;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.products.domain.Effort;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Regular CRUD view on the effort entity. The grid and edition dialog are optimized for usability.
 */
@PageTitle("products-efforts")
class EffortsView extends ItemView<Effort> {
    public static final String ASSIGNMENT = "assignement";
    public static final String ASSIGNMENT_LABEL = "Assignment";
    public static final String COLLABORATOR = "collaborator";
    public static final String COLLABORATOR_LABEL = "Collaborator";
    private static final String CONTRACT_ID = "contractId";
    private static final String CONTRACT_ID_LABEL = "Contract ID";
    public static final String DURATION = "duration";
    public static final String DURATION_LABEL = "Duration";


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
            dataView().refreshAll();
        }

        public void text(String text) {
            this.text = text;
            dataView().refreshAll();
        }

        public boolean test(@NotNull Effort entity) {
            return ItemFilter.matches(entity.contractId(), contractId) &&
                ItemFilter.matches(Objects.nonNull(entity.assignment()) ? entity.assignment().id() : null, assignmentId) &&
                ItemFilter.matches(Objects.nonNull(entity.assignment()) ? entity.assignment().collaboratorId() : null, collaboratorId) &&
                ItemFilter.matches(entity.text(), text);
        }
    }

    static class EffortForm extends ItemForm<Effort, EffortsView> {

        EffortForm(@NotNull EffortsView parent) {
            super(parent);
            addTabAt("details", details(), 0);
            addTabAt("text", textForm(), 1);
        }

        protected FormLayout details() {
            TextField assignment = VaadinUtils.createTextField(ASSIGNMENT_LABEL, ASSIGNMENT, true, false);
            TextField collaborator = VaadinUtils.createTextField(COLLABORATOR_LABEL, COLLABORATOR, true, false);
            TextField collaboratorId = VaadinUtils.createTextField("Collaborator ID", "collaborator id", true, false);
            TextField contractId = new TextField(CONTRACT_ID_LABEL, CONTRACT_ID);
            DatePicker date = VaadinUtils.createDatePicker(DATE_LABEL);
            IntegerField duration = new IntegerField(DURATION_LABEL, DURATION);

            FormLayout form = new FormLayout();
            form.add(assignment, collaborator, collaboratorId, contractId, date, duration);

            binder().bindReadOnly(assignment, o -> o.assignment().id());
            binder().bindReadOnly(collaborator, o -> o.assignment().name());
            binder().bindReadOnly(collaboratorId, o -> o.assignment().collaboratorId());
            binder().bind(contractId, Effort::contractId, Effort::contractId);
            binder().bind(date, Effort::date, Effort::date);
            binder().bind(duration, Effort::duration, Effort::duration);
            return form;
        }

        @Override
        protected Effort createOrUpdateInstance(Effort entity) throws ValidationException {
            return Objects.isNull(entity) ? new Effort() : entity;
        }
    }

    public EffortsView(@NotNull ProductsBoundedDomainUi domain, @NotNull Mode mode) {
        super(Effort.class, domain, domain.efforts(), new EffortFilter(), mode);
        form(() -> new EffortForm(this));
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(o -> Objects.nonNull(o.assignment()) ? o.assignment().id() : null).setKey(ASSIGNMENT).setHeader(ASSIGNMENT_LABEL).setAutoWidth(true)
            .setResizable(true).setSortable(true);
        grid.addColumn(o -> Objects.nonNull(o.assignment()) ? o.assignment().name() : null).setKey(COLLABORATOR).setHeader(COLLABORATOR_LABEL)
            .setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::date).setKey(DATE).setHeader(DATE_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::duration).setKey(DURATION).setHeader(DURATION_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Effort::contractId).setKey(CONTRACT_ID).setHeader(CONTRACT_ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
    }
}
