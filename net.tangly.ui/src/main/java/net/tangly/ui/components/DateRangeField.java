/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.ui.components;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import net.tangly.core.DateRange;

public class DateRangeField extends CustomField<DateRange> {
    private final DatePicker from;
    private final DatePicker to;

    DateRangeField(String label) {
        setLabel(label);
        from = new DatePicker("From");
        to = new DatePicker("To");
        add(from, to);
    }

    @Override
    protected DateRange generateModelValue() {
        return DateRange.of(from.getValue(), to.getValue());
    }

    @Override
    protected void setPresentationValue(DateRange dateRange) {
        from.setValue(dateRange.from());
        to.setValue(dateRange.to());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        from.setReadOnly(readOnly);
        to.setReadOnly(readOnly);
    }

    @Override
    public void clear() {
        from.clear();
        to.clear();
    }
}
