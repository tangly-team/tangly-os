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

package net.tangly.ui.components;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import net.tangly.core.DateRange;

import java.time.LocalDate;
import java.util.Objects;

public class DateRangePicker extends CustomField<DateRange> {
    private final Button rangeText;
    private DateRange range;

    public DateRangePicker(String label, LocalDate lowerDate, LocalDate upperDate) {
        if (Objects.nonNull(label)) {
            this.setLabel(label);
        }
        range = DateRange.of(lowerDate, upperDate);
        rangeText = new Button();
        setPresentationValue(range);
        rangeText.addClickListener(e -> create().open());
        add(rangeText);
    }

    @Override
    protected DateRange generateModelValue() {
        return range;
    }

    @Override
    protected void setPresentationValue(DateRange range) {
        String fromText = Objects.isNull(range.from()) ? (range.isInfinite() ? "" : "-") : range.from().toString();
        String toText = Objects.isNull(range.to()) ? (range.isInfinite() ? "" : "-") : range.to().toString();
        String text = "<div style=\"font-size:0.75em;\">" + fromText + " <br> " + toText + "</div>";
        rangeText.setIcon(new Html(text));
    }

    private Dialog create() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Date Range");

        DatePicker from = new DatePicker();
        from.setPlaceholder("Start date");
        from.setClearButtonVisible(true);
        var range = getValue();
        if (range.from() != null) {
            from.setValue(range.from());
        }

        DatePicker to = new DatePicker();
        to.setClearButtonVisible(true);
        to.setPlaceholder("End date");
        to.setClearButtonVisible(true);
        if (range.to() != null) {
            to.setValue(range.to());
        }

        dialog.add(new VerticalLayout(from, to));
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()));
        dialog.getFooter().add(new Button("OK", e -> {
            this.range = DateRange.of(from.getValue(), to.getValue());
            setPresentationValue(this.range);
            updateValue();
            dialog.close();
        }));
        return dialog;
    }
}
