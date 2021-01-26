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

import java.time.LocalDate;
import java.util.List;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.Effort;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.commons.domain.ui.Cmd;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class CmdCreateEffort extends Dialog implements Cmd {
    private final Binder<Effort> binder;
    private final Effort effort;
    private final Assignment assignment;
    private final ProductsBoundedDomain domain;

    public CmdCreateEffort(@NotNull Assignment assignment, @NotNull ProductsBoundedDomain domain) {
        this.assignment = assignment;
        this.domain = domain;
        binder = new Binder<>();
        effort = new Effort();
        effort.assignment(assignment);
        effort.date(LocalDate.now());
        List<String> contractIds = assignment.product().contractIds();
        if (contractIds.size() == 1) {
            effort.contractId(contractIds.get(0));
        }
    }

    @Override
    public void execute() {
        setResizable(true);
        add(create(domain));
        open();
    }

    private FormLayout create(@NotNull ProductsBoundedDomain domain) {
        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);

        TextField assignment = VaadinUtils.createTextField("Assignment", "assignment", true, false);
        TextField collaborator = VaadinUtils.createTextField("Collaborator", "collaborator", true, false);
        TextField collaboratorId = VaadinUtils.createTextField("Collaborator ID", "collaborator id", true, false);
        DatePicker date = VaadinUtils.createDatePicker("Date");
        IntegerField duration = new IntegerField("Duration", "duration");
        duration.setRequiredIndicatorVisible(true);
        duration.setHasControls(true);
        duration.setMin(1);
        TextField contract = null;
        ComboBox<String> contracts = null;
        List<String> contractIds = effort.assignment().product().contractIds();
        if (contractIds.size() == 1) {
            contract = VaadinUtils.createTextField("Contract", "contract", true, false);
        } else {
            contracts = new ComboBox<>("Contract", contractIds);
            contracts.setRequired(true);
        }
        TextArea text = new TextArea("Text", "text");

        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            try {
                binder.writeBean(effort);
                domain.realm().registerOid(effort);
                domain.realm().efforts().update(effort);
            } catch (ValidationException validationException) {
                validationException.printStackTrace();
            }
            this.close();
        });
        Button cancel = new Button("Cancel", e -> this.close());

        form.add(assignment, collaborator, collaboratorId, date, (contract != null) ? contract : contracts, duration);
        form.add(text, 3);
        form.add(new HtmlComponent("br"), new HorizontalLayout(execute, cancel));

        binder.bind(assignment, o -> o.assignment().id(), null);
        binder.bind(collaborator, o -> o.assignment().name(), null);
        binder.bind(collaboratorId, o -> o.assignment().collaboratorId(), null);
        binder.bind(date, Effort::date, Effort::date);
        binder.bind(duration, Effort::duration, Effort::duration);
        if (contract != null) {
            binder.bind(contract, Effort::contractId, Effort::contractId);
        } else {
            binder.bind(contracts, Effort::contractId, Effort::contractId);
        }
        binder.bind(text, Effort::text, Effort::text);
        binder.readBean(effort);
        return form;
    }
}
