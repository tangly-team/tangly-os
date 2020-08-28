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

package net.tangly.commons.vaadin;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.core.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityField extends CustomField<Entity> {
    private boolean readonly;
    private final TextField oid;
    private final TextField id;
    private final TextField name;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final TextArea text;

    public EntityField() {
        super(null);
        oid = new TextField("Oid", "oid");
        id = new TextField("Id", "id");
        name = new TextField("Name", "name");
        fromDate = new DatePicker("From Date");
        toDate = new DatePicker("To Date");
        text = new TextArea("Text", "text");
        text.setWidthFull();
        Details textDetails = new Details("Text", text);

        add(new HorizontalLayout(oid, id, name));
        add(new HorizontalLayout(fromDate, toDate));
        add(textDetails);
    }

    @Override
    protected Entity generateModelValue() {
        return null;
    }

    @Override
    protected void setPresentationValue(Entity entity) {
        if (entity == null) {
            clear();
        }
    }

    public <T extends Entity> void bind(@NotNull Binder<T> binder) {
        binder.bind(oid, o -> Long.toString(o.oid()), null);
        binder.bind(id, Entity::id, Entity::id);
        binder.bind(name, Entity::name, Entity::name);
        binder.forField(fromDate)
                .withValidator(from -> (from == null) || (toDate.getValue() == null) || (from.isBefore(toDate.getValue())), "From date must be before to date")
                .bind(Entity::fromDate, Entity::fromDate);
        binder.forField(toDate)
                .withValidator(to -> (to == null) || (fromDate.getValue() == null) || (to.isAfter(fromDate.getValue())), "To date must be after from date")
                .bind(Entity::toDate, Entity::toDate);
        binder.bind(text, Entity::text, Entity::text);
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
