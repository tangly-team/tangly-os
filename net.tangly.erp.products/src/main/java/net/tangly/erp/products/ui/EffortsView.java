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

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import net.tangly.app.ApplicationView;
import net.tangly.commons.lang.Strings;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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

    /**
     * Creates the form to edit or create an effort entity. The form has two entry points when creating a new effort.
     * An assignment selection triggers the update of all derived fields in the effort form. These fields are set to read-only.
     * If the assignment is empty, the collaborator selection triggers the update of the assignment list.
     * The two checkboxes activePeriod and restrictedUser control the content of the collaborator and assignment field list.
     * If the restricted user is checked, only the current user is available in the collaborator list and the assignment list is filtered accordingly.
     * If the active period is checked, only the assignments active `now()` are available in the assignment list.
     * <p>The user has restricted rights. The user can only see and edit efforts belonging to him.
     * The username is set to the registered user and is not editable. The date is set to today. Only is assignments are available for selection.</p>
     * <p>The user has regular rights. The username is set when creating a new effor. The date is set to today.</p>
     */
    static class EffortForm extends ItemForm<Effort, EffortsView> {

        EffortForm(@NotNull EffortsView parent) {
            super(parent);
            addTabAt("details", details(), 0);
            addTabAt("text", textForm(), 1);
        }

        protected FormLayout details() {
            TextField collaboratorId = VaadinUtils.createTextField("Collaborator ID", "collaborator id", true, false);
            collaboratorId.setReadOnly(true);
            DatePicker closedPeriod = VaadinUtils.createDatePicker("Closed Period", true, true);
            closedPeriod.setReadOnly(true);

            Checkbox restrictedUser = new Checkbox("Restrict to User", isRestrictedUser());
            restrictedUser.setReadOnly(isRestrictedUser());

            Checkbox activePeriod = new Checkbox("Active Period", true);

            ComboBox<String> contractId = new ComboBox<>(CONTRACT_ID_LABEL);
            contractId.setRequired(true);

            DatePicker date = VaadinUtils.createDatePicker(DATE_LABEL);
            date.setValue(LocalDate.now());
            date.setRequired(true);

            IntegerField duration = new IntegerField(DURATION_LABEL, DURATION);
            duration.setMin(0);
            duration.setRequired(true);

            ComboBox<String> collaborator = new ComboBox<>(COLLABORATOR_LABEL);
            collaborator.setRequired(true);
            collaborator.setReadOnly(isRestrictedUser());
            collaborator.setClearButtonVisible(true);

            ComboBox<Assignment> assignment = new ComboBox<>(ASSIGNMENT_LABEL);
            assignment.setItemLabelGenerator(o -> "%s - %s".formatted(o.id(), o.name()));
            assignment.setRequired(true);
            assignment.setClearButtonVisible(true);

            activePeriod.addValueChangeListener(e -> {
                updateAssignment(assignment, collaborator.getValue(), activePeriod.getValue());
                if (Objects.isNull(assignment.getValue())) {
                    // cut logic to the form update
                    updateCollaborator(collaborator, assignment.getValue(), restrictedUser.getValue(), activePeriod.getValue());
                }
            });
            restrictedUser.addValueChangeListener(e -> {
                updateAssignment(assignment, collaborator.getValue(), activePeriod.getValue());
                if (Objects.isNull(assignment.getValue())) {
                    // cut logic to the form update
                    updateCollaborator(collaborator, assignment.getValue(), restrictedUser.getValue(), activePeriod.getValue());
                }
            });
            collaborator.addValueChangeListener(e -> {
                if (Objects.isNull(assignment.getValue())) {
                    // cut logic to the form update
                    updateAssignment(assignment, collaborator.getValue(), activePeriod.getValue());
                }
            });
            assignment.addValueChangeListener(e -> {
                Assignment currentAssignment = e.getValue();
                if (Objects.nonNull(currentAssignment)) {
                    collaboratorId.setValue(currentAssignment.collaboratorId());
                    closedPeriod.setValue(currentAssignment.closedPeriod());
                } else {
                    collaboratorId.clear();
                    closedPeriod.clear();
                }
                collaboratorId.setReadOnly(Objects.isNull(currentAssignment));
                closedPeriod.setReadOnly(Objects.isNull(currentAssignment));
                updateCollaborator(collaborator, currentAssignment, restrictedUser.getValue(), activePeriod.getValue());
                updateContractId(contractId, currentAssignment);
            });

            updateCollaborator(collaborator, assignment.getValue(), restrictedUser.getValue(), activePeriod.getValue());
            updateAssignment(assignment, collaborator.getValue(), activePeriod.getValue());
            updateContractId(contractId, assignment.getValue());

            if (creating()) {
                date.setValue(LocalDate.now());
                collaborator.setValue(username());
            }

            FormLayout form = new FormLayout();
            form.add(restrictedUser, activePeriod, assignment, collaborator, collaboratorId, contractId, closedPeriod, date, duration);

            binder().bindReadOnly(assignment, o -> o.assignment());
            binder().bindReadOnly(collaborator, o -> o.assignment().name());
            binder().bindReadOnly(collaboratorId, o -> o.assignment().collaboratorId());
            binder().bindReadOnly(closedPeriod, o -> o.assignment().closedPeriod());
            binder().bind(contractId, Effort::contractId, Effort::contractId);
            binder().forField(date).withValidator(
                o -> (Objects.isNull(value()) || Objects.isNull(value().assignment()) || Objects.isNull(value().assignment().closedPeriod()) ||
                    value().assignment().closedPeriod().isBefore(o)), "date must be after closed period date").bind(Effort::date, Effort::date);
            binder().bind(duration, Effort::duration, Effort::duration);
            return form;
        }

        @Override
        protected Effort createOrUpdateInstance(Effort entity) throws ValidationException {
            return Objects.isNull(entity) ? new Effort() : entity;
        }

        private boolean isRestrictedUser() {
            return ApplicationView.user().accessRightsFor(parent().domain().name()).stream().anyMatch(o -> o.right() == AccessRightsCode.restrictedUser);
        }

        private String username() {
            return ApplicationView.username();
        }


        private void updateContractId(ComboBox<String> contractId, Assignment assignment) {
            List<String> contractIds = Objects.nonNull(assignment) ? assignment.product().contractIds() : Collections.emptyList();
            String currentContractId = contractId.getValue();
            contractId.setItems(contractIds);
            contractId.setReadOnly(Objects.nonNull(assignment) && (contractIds.size() == 1));
            if (Objects.nonNull(assignment) && (contractIds.size() == 1)) {
                contractId.setValue(contractIds.getFirst());
            } else {
                if (contractIds.contains(contractId)) {
                    contractId.setValue(currentContractId);
                } else {
                    contractId.clear();
                }
            }
        }

        private void updateAssignment(ComboBox<Assignment> assignment, String collaborator, boolean activePeriod) {
            Assignment currentAssignment = assignment.getValue();
            List<Assignment> assignments = assignmentFor(collaborator, activePeriod);
            assignment.setItems(assignments);
            if (assignments.contains(currentAssignment)) {
                assignment.setValue(currentAssignment);
            } else {
                assignment.clear();
            }
        }

        private void updateCollaborator(ComboBox<String> collaborator, Assignment assignment, boolean restrictedUser, boolean activePeriod) {
            collaborator.setReadOnly(Objects.nonNull(assignment));
            String currentCollaborator = collaborator.getValue();
            List<String> collaborators = usernamesFor(assignment, restrictedUser, activePeriod);
            collaborator.setItems(collaborators);
            if (Objects.nonNull(assignment)) {
                collaborator.setValue(assignment.name());
            } else if (collaborators.contains(currentCollaborator)) {
                collaborator.setValue(currentCollaborator);
            } else {
                collaborator.clear();
            }
        }

        private List<Assignment> assignmentFor(@NotNull String username, boolean active) {
            return parent().domain().realm().assignments().items().stream()
                .filter(o -> (Strings.isNullOrBlank(username) || o.name().equals(username)) && (!active || o.range().isActive())).toList();
        }

        private List<String> usernamesFor(Assignment assignment, boolean restricted, boolean active) {
            return Objects.nonNull(assignment) ? List.of(assignment.name()) : restricted ? List.of(username()) :
                parent().domain().realm().assignments().items().stream().filter(o -> !active || o.range().isActive()).map(Assignment::name).distinct().toList();
        }
    }

    public EffortsView(@NotNull ProductsBoundedDomainUi domain, @NotNull Mode mode) {
        super(Effort.class, domain, domain.efforts(), new EffortFilter(), mode);
        form(() -> new EffortForm(this));
        init();
    }

    @Override
    public ProductsBoundedDomain domain() {
        return (ProductsBoundedDomain) super.domain();
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
