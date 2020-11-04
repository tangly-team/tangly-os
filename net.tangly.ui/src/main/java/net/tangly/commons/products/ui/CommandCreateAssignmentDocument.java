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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.commons.vaadin.VaadinUtils;

public class CommandCreateAssignmentDocument extends Dialog {
    private final TextField assignmentName;
    private final TextField collaboratorName;
    private final DatePicker fromDate;
    private final DatePicker toDate;

    public CommandCreateAssignmentDocument(Assignment assignment, ProductsBusinessLogic logic) {
        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);

        assignmentName = VaadinUtils.createTextField("Assignment", "assignment name", true);
        VaadinUtils.setValue(assignmentName, assignment.id());
        collaboratorName = VaadinUtils.createTextField("Collaborator", "collaborator name", true);
        VaadinUtils.setValue(collaboratorName, assignment.name());

        fromDate = VaadinUtils.createDatePicker("From");
        toDate = VaadinUtils.createDatePicker("To");

        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            logic.port().exportEffortsDocument(assignment, fromDate.getValue(), toDate.getValue());
            this.close();
        });
        Button cancel = new Button("Cancel", e -> this.close());

        form.add(assignmentName, collaboratorName, new HtmlComponent("br"), fromDate, toDate, new HtmlComponent("br"), new HorizontalLayout(execute, cancel));
        add(form);
        open();
    }
}
