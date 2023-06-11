/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.components;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import net.tangly.ui.asciidoc.AsciiDocField;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a composite field for the properties of an entity object or a subclass of an entity abstraction.
 *
 * @param <T> Type of instances to be displayed
 */
public class EntityField<T extends HasOid & HasId & HasName & HasText & HasTimeInterval> extends CustomField<T> {
    private final IntegerField oid;
    private final TextField id;
    private final TextField name;
    private final DatePicker from;
    private final DatePicker to;
    private final AsciiDocField text;

    public EntityField() {
        oid = new IntegerField(EntityView.OID, EntityView.OID_LABEL);
        oid.setReadOnly(true);
        id = new TextField(EntityView.ID, EntityView.ID_LABEL);
        name = new TextField(EntityView.NAME, EntityView.NAME_LABEL);
        name.setClearButtonVisible(true);
        from = new DatePicker(EntityView.FROM_LABEL);
        to = new DatePicker(EntityView.TO_LABEL);
        text = new AsciiDocField(EntityView.TEXT_LABEL);
        text.setWidthFull();
        add(new HorizontalLayout(oid, id, name, from, to));
        add(text);
    }

    @Override
    protected T generateModelValue() {
        return null;
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
        oid.setReadOnly(readOnly);
        id.setReadOnly(readOnly);
        name.setReadOnly(readOnly);
        from.setReadOnly(readOnly);
        to.setReadOnly(readOnly);
        text.setReadOnly(readOnly);
    }

    @Override
    public void clear() {
        super.clear();
        oid.clear();
        id.clear();
        name.clear();
        from.clear();
        to.clear();
        text.clear();
    }

    public void bind(@NotNull Binder<T> binder, boolean isMutable) {
        binder.bindReadOnly(oid, o -> (int) o.oid());
        binder.bind(id, HasId::id, isMutable ? HasId::id : null);
        binder.bind(name, HasName::name, isMutable ? HasName::name : null);
        binder.forField(from).withValidator(from -> (from == null) || (to.getValue() == null) || (from.isBefore(to.getValue())), "From date must be before to date")
            .bind(HasTimeInterval::from, isMutable ? HasTimeInterval::from : null);
        binder.forField(to).withValidator(to -> (to == null) || (from.getValue() == null) || (to.isAfter(from.getValue())), "To date must be after from date")
            .bind(HasTimeInterval::to, isMutable ? HasTimeInterval::to : null);
        binder.bind(text, HasText::text, isMutable ? HasText::text : null);
    }
}
