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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static java.util.FormatProcessor.FMT;

public class CmdCreateEffortsReport implements Cmd {
    public final String TITLE = "Create Assignment Document";
    private final TextField assignmentName;
    private final TextField collaboratorName;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final Select<ChronoUnit> units;
    private final Checkbox perMonth;
    private final TextField filename;
    private final Button propose;
    private Button execute;
    private final transient Assignment assignment;
    private final transient ProductsBoundedDomain domain;
    private Dialog dialog;

    public CmdCreateEffortsReport(@NotNull Assignment assignment, @NotNull ProductsBoundedDomain domain) {
        this.assignment = assignment;
        this.domain = domain;
        assignmentName = VaadinUtils.createTextField("Assignment", "assignment name", true);
        VaadinUtils.setValue(assignmentName, assignment.id());
        collaboratorName = VaadinUtils.createTextField("Collaborator", "collaborator name", true);
        VaadinUtils.setValue(collaboratorName, assignment.name());
        fromDate = VaadinUtils.createDatePicker("From");
        toDate = VaadinUtils.createDatePicker("To");
        units = new Select<>();
        units.setItems(ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS);
        units.setValue(ChronoUnit.HOURS);
        filename = VaadinUtils.createTextField("Filename", "filename");
        filename.setRequired(true);
        filename.addValueChangeListener(_ -> execute.setEnabled(isExecuteEnabled()));
        propose = new Button("Propose", VaadinIcon.COGS.create(), _ -> filename.setValue(filename(assignment.id(), assignment.name(), fromDate.getValue(), toDate.getValue())));
        fromDate.addValueChangeListener(_ -> {
            validateOnChangedDate();
            execute.setEnabled(isExecuteEnabled());
            execute.setEnabled(isExecuteEnabled());
        });
        toDate.addValueChangeListener(_ -> {
            validateOnChangedDate();
        });
        perMonth = new Checkbox("Reports Splitted Per month");
        perMonth.addValueChangeListener(event -> {
            fromDate.setRequired(perMonth.getValue());
            toDate.setRequired(perMonth.getValue());
            filename.setEnabled(!perMonth.getValue());
            filename.setRequired(!perMonth.getValue());
            propose.setEnabled(!perMonth.getValue());
            execute.setEnabled(isExecuteEnabled());
        });
    }

    @Override
    public void execute() {
        dialog = new Dialog();
        dialog.setHeaderTitle(TITLE);
        dialog.setWidth("40em");
        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        execute = new Button("Execute", VaadinIcon.COGS.create(), _ -> {
            if (perMonth.getValue()) {
                domain.port().exportEffortsDocumentsSplittedPerMonth(assignment, YearMonth.from(fromDate.getValue()), YearMonth.from(toDate.getValue()), filename.getValue(),
                    units.getValue());
            } else {
                domain.port().exportEffortsDocument(assignment, fromDate.getValue(), toDate.getValue(), filename.getValue(), units.getValue());
            }
            close();
        });
        execute.setEnabled(false);
        Button cancel = new Button("Cancel", _ -> dialog.close());
        form.add(assignmentName, collaboratorName,
            new HtmlComponent("br"), fromDate, toDate, units, perMonth,
            new HtmlComponent("br"), filename, propose);
        dialog.add(form);
        dialog.getFooter().add(execute, cancel);
        dialog.open();
    }

    @Override
    public Dialog dialog() {
        return dialog;
    }

    protected void close() {
        dialog.close();
        dialog = null;
    }

    private void validateOnChangedDate() {
        if (!validateDateInterval(fromDate.getValue(), toDate.getValue())) {
            fromDate.setErrorMessage("From date shall be small than to date");
            fromDate.setInvalid(true);
            toDate.setErrorMessage("From date shall be small than to date");
            toDate.setInvalid(true);
        } else {
            fromDate.setErrorMessage(null);
            fromDate.setInvalid(false);
            toDate.setErrorMessage(null);
            toDate.setInvalid(false);
        }
    }

    private static boolean validateDateInterval(LocalDate from, LocalDate to) {
        return Objects.isNull(from) || Objects.isNull(to) || !to.isBefore(from);
    }

    private boolean isExecuteEnabled() {
        return (!perMonth.getValue() && validateDateInterval(fromDate.getValue(), toDate.getValue()) && !filename.isEmpty())
            || (perMonth.getValue() && Objects.nonNull(fromDate.getValue()) && Objects.nonNull(toDate.getValue()) && validateDateInterval(fromDate.getValue(), toDate.getValue()));
    }

    private String filename(@NotNull String assignment, @NotNull String collaborator, LocalDate from, LocalDate to) {
        YearMonth generated = (Objects.nonNull(to)) ? YearMonth.from(to) : YearMonth.now();
        String generatedText = FMT."\{generated.getYear()}-%02d\{generated.getMonthValue()}";

        String dateText;
        if (Objects.nonNull(from) && Objects.nonNull(to) && (YearMonth.from(from).equals(YearMonth.from(to)))) {
            dateText = YearMonth.from(to).getMonth().toString();
            dateText = STR."\{dateText.substring(0, 1).toUpperCase()}\{dateText.substring(1).toLowerCase()}";
        } else {
            dateText = STR."\{Objects.isNull(from) ? "none" : from.toString()}-\{Objects.isNull(to) ? "none" : to.toString()}";
        }
        return STR."\{generatedText}-\{assignment}-\{collaborator}-\{dateText}\{AsciiDoctorHelper.ASCIIDOC_EXT}";
    }
}
