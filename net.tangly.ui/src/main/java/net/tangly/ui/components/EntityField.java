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

package net.tangly.ui.components;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.*;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a composite field for the properties of an entity object or a subclass of an entity abstraction.
 *
 * @param <T> Type of instances to be displayed
 */
public class EntityField<T extends Entity> extends CustomField<T> {
    private final IntegerField oid;
    private final TextField id;
    private final TextField name;
    private final DatePicker from;
    private final DatePicker to;

    public EntityField(String label) {
        setLabel(label);
        oid = new IntegerField(EntityView.OID, EntityView.OID_LABEL);
        oid.setReadOnly(true);
        id = new TextField(EntityView.ID, EntityView.ID_LABEL);
        name = new TextField(EntityView.NAME, EntityView.NAME_LABEL);
        name.setClearButtonVisible(true);
        from = new DatePicker(EntityView.FROM_LABEL);
        to = new DatePicker(EntityView.TO_LABEL);
        add(new HorizontalLayout(oid, id, name, from, to));
    }

    @Override
    protected T generateModelValue() {
        return getValue();
    }

    @Override
    protected void setPresentationValue(T entity) {
        if (entity == null) {
            clear();
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        id.setReadOnly(readOnly);
        name.setReadOnly(readOnly);
        from.setReadOnly(readOnly);
        to.setReadOnly(readOnly);
    }

    @Override
    public void clear() {
        super.clear();
        oid.clear();
        id.clear();
        name.clear();
        from.clear();
        to.clear();
    }

    public void clearOid() {
        oid.clear();
    }

    public <V extends MutableEntity> void bindMutable(@NotNull Binder<V> binder) {
        binder.bindReadOnly(oid, o -> (int) o.oid());
        binder.bind(id, HasMutableId::id, HasMutableId::id);
        binder.bind(name, HasMutableName::name, HasMutableName::name);
        binder.forField(from).withValidator(o -> (o == null) || (to.getValue() == null) || (o.isBefore(to.getValue())), "From date must be before to date")
            .bind(HasMutableDateRange::from, HasMutableDateRange::from);
        binder.forField(to).withValidator(o -> (o == null) || (from.getValue() == null) || (o.isAfter(from.getValue())), "To date must be after from date")
            .bind(HasMutableDateRange::to, HasMutableDateRange::to);
    }

    public void bind(@NotNull Binder<T> binder) {
        binder.bindReadOnly(oid, o -> (int) o.oid());
        binder.bindReadOnly(id, HasId::id);
        binder.bindReadOnly(name, HasName::name);
        binder.forField(from).withValidator(o -> (o == null) || (to.getValue() == null) || (o.isBefore(to.getValue())), "From date must be before to date")
            .bind(HasDateRange::from, null);
        binder.forField(to).withValidator(o -> (o == null) || (from.getValue() == null) || (o.isAfter(from.getValue())), "To date must be after from date")
            .bind(HasDateRange::to, null);
    }

// region Entity user interface field accessors (read-only instances)

    public long oid() {
        return oid.getValue();
    }

    public String id() {
        return id.getValue();
    }

    public String name() {
        return name.getValue();
    }

    public DateRange dateRange() {
        return DateRange.of(from.getValue(), to.getValue());
    }

    // endregion
}
