/*
 * Copyright 2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.ui.daterange;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateRangePicker extends CustomField<DateRange> {

    protected DatePicker startDatePicker;
    protected DatePicker endDatePicker;
    protected DateRangeShortcut contextMenu;
    protected HorizontalLayout horizontalLayout = new HorizontalLayout();

    public DateRangePicker(String label, String startLabel, String endLabel, boolean withShortcuts) {
        setupComponent(label, startLabel, null, endLabel, null, withShortcuts);
        add(horizontalLayout);
    }

    public DateRangePicker(String label, String startLabel, LocalDate startDate, String endLabel, LocalDate endDate, boolean withShortcuts) {
        setupComponent(label, startLabel, startDate, endLabel, endDate, withShortcuts);
        add(horizontalLayout);
    }

    public void isGrouped() {
        horizontalLayout.setSpacing(false);
    }

    private void addValueChange() {
        startDatePicker.addValueChangeListener(change -> {
            LocalDate startDate = change.getValue();
            if (startDate != null) {
                LocalDate endDate = this.getEndDate();
                endDatePicker.setMin(startDate.plusDays(1));
                if (endDate == null) {
                    endDatePicker.setOpened(true);
                }
            } else {
                endDatePicker.setMin(null);
            }
            DateRange.between(startDate, endDatePicker.getValue());
        });

        endDatePicker.addValueChangeListener(change -> {
            LocalDate endDate = change.getValue();
            if (endDate != null) {
                startDatePicker.setMax(endDate.minusDays(1));
            } else {
                startDatePicker.setMax(null);
            }
            DateRange.between(startDatePicker.getValue(), endDate);
        });
    }

    private void setupComponent(String label, String startLabel, LocalDate startDate, String endLabel, LocalDate endDate, boolean withShortcuts) {
        if (label != null && !label.isEmpty()) {
            setLabel(label);
        }
        startDatePicker = new DatePicker(startLabel);
        endDatePicker = new DatePicker(endLabel);
        if (startDate != null) {
            startDatePicker.setValue(startDate);
        }
        if (endDate != null) {
            endDatePicker.setValue(endDate);
        }
        horizontalLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        horizontalLayout.add(startDatePicker, endDatePicker);
        if (withShortcuts) {
            createShortcuts(horizontalLayout);
        }
        addValueChange();
    }

    public void createShortcuts(HorizontalLayout mainComponent) {
        Button shortcutDate = new Button(new Icon(VaadinIcon.ARROW_DOWN));
        shortcutDate.getElement().setAttribute("title", "Right-click to select the range");
        mainComponent.setVerticalComponentAlignment(FlexComponent.Alignment.BASELINE, shortcutDate);
        LocalDate today = LocalDate.now();
        DateRangeShortcut dateRangeShortcut = new DateRangeShortcut(shortcutDate);
        dateRangeShortcut.addShortcut("today", field -> setRange(today, today.plusDays(1)));
        dateRangeShortcut.addShortcut("this week", field -> setRange(today.with(DayOfWeek.MONDAY), today.plusWeeks(1).with(DayOfWeek.SUNDAY)));
        dateRangeShortcut.addShortcut("this month", field -> setRange(today.with(DayOfWeek.MONDAY), today.plusMonths(1).with(DayOfWeek.SUNDAY)));
        dateRangeShortcut.addShortcut("add one year", field -> setRange(today.with(DayOfWeek.MONDAY), today.plusYears(1).with(DayOfWeek.SUNDAY)));
        MenuItem item = dateRangeShortcut.addItem("clear", field -> this.clear());
        item.getElement().getStyle().set("color", "red");
        dateRangeShortcut.addShortcut(item);
        mainComponent.add(shortcutDate);
    }


    private DateRange setRange(LocalDate beginDate, LocalDate endDate) {
        DateRange dateRange = DateRange.between(beginDate, endDate);
        this.startDatePicker.setValue(beginDate);
        this.endDatePicker.setValue(endDate);
        this.endDatePicker.setOpened(false);
        this.startDatePicker.setOpened(false);
        return dateRange;
    }

    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public DatePicker getEndDatePicker() {
        return endDatePicker;
    }


    public LocalDate getEndDate() {
        return this.endDatePicker.getValue();
    }

    public LocalDate getStartDate() {
        return this.startDatePicker.getValue();
    }

    @Override
    public void clear() {
        this.startDatePicker.clear();
        this.endDatePicker.clear();
        DateRange.between(this.startDatePicker.getValue(), this.endDatePicker.getValue());
        this.addValueChange();
    }


    public DateRangeShortcut getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(DateRangeShortcut contextMenu) {
        this.contextMenu = contextMenu;
    }

    @Override
    protected DateRange generateModelValue() {
        return DateRange.between(startDatePicker.getValue(), endDatePicker.getValue());
    }

    @Override
    protected void setPresentationValue(DateRange dateRange) {
        this.setRange(dateRange.getBeginDate(), dateRange.getEndDate());
    }


}
