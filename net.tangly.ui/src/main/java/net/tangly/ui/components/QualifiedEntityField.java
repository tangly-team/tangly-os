/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.components;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.QualifiedEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Tag("tangly-field-qualified-entity")
public class QualifiedEntityField<T extends QualifiedEntity> extends CustomField<T> {
    private boolean readonly;
    private final TextField oid;
    private final TextField id;
    private final TextField name;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final TextArea text;

    public QualifiedEntityField() {
        super(null);
        oid = new TextField("Oid", "oid");
        id = new TextField("Id", "id");
        name = new TextField("Name", "name");
        fromDate = new DatePicker("From Date");
        toDate = new DatePicker("To Date");
        text = new TextArea("Text", "text");
        add(new HorizontalLayout(oid, id, name));
        add(new HorizontalLayout(fromDate, toDate));
        add(text);
    }

    public void addEntityComponentsTo(@NotNull FormLayout form) {
        Details textDetails = new Details("text", text);
        textDetails.addThemeVariants(DetailsVariant.SMALL);
        form.add(oid, id, name, fromDate, toDate, new HtmlComponent("br"), textDetails);
        form.setColspan(textDetails, 3);
    }

    @Override
    protected T generateModelValue() {
        return null;
    }

    @Override
    protected void setPresentationValue(T entity) {
        if (Objects.isNull(entity)) {
            clear();
        }
    }

    public void bind(@NotNull Binder<T> binder) {
        binder.bind(oid, o -> Long.toString(o.oid()), null);
        binder.bind(id, QualifiedEntity::id, QualifiedEntity::id);
        binder.bind(name, QualifiedEntity::name, QualifiedEntity::name);
        binder.forField(fromDate)
                .withValidator(from -> (from == null) || (toDate.getValue() == null) || (from.isBefore(toDate.getValue())), "From date must be before to date")
                .bind(QualifiedEntity::fromDate, QualifiedEntity::fromDate);
        binder.forField(toDate)
                .withValidator(to -> (to == null) || (fromDate.getValue() == null) || (to.isAfter(fromDate.getValue())), "To date must be after from date")
                .bind(QualifiedEntity::toDate, QualifiedEntity::toDate);
        binder.bind(text, QualifiedEntity::text, QualifiedEntity::text);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        oid.setReadOnly(readOnly);
        id.setReadOnly(readOnly);
        name.setReadOnly(readOnly);
        fromDate.setReadOnly(readOnly);
        toDate.setReadOnly(readOnly);
        text.setReadOnly(readOnly);
    }

    @Override
    public void clear() {
        super.clear();
        oid.clear();
        id.clear();
        name.clear();
        fromDate.clear();
        toDate.clear();
        text.clear();
    }
}
