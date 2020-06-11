/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import java.util.Optional;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.Tag;
import net.tangly.bus.core.TagType;
import net.tangly.bus.core.TagTypeRegistry;

/**
 * The view displays the tags of an entity. The whole behavior is similar of a CRUD component with a data provider only retrieving the set of tags of
 * an entity. A tag can be
 * <ul>
 *     <li>Viewed in the grid and if selected in the fields below the grid.</li>
 *     <li>The selected tag can be deleted.</li>
 *     <li>A new tag can be added.</li>
 * </ul>
 */
public class TagForm implements CrudForm<Tag>, CrudActionsListener<Tag> {
    private HasTags hasTags;
    private Binder<Entity> binder;
    private transient TagTypeRegistry registry;

    @Override
    public FormLayout createForm(Operation operation, Tag entity) {
        ComboBox<String> namespace = new ComboBox<>("Namespace");
        namespace.setEnabled(false);
        ComboBox<String> name = new ComboBox<>("Name");
        TextField value = new TextField("Value");
        namespace.addValueChangeListener(event -> {
            if (operation == Operation.CREATE) {
                name.setValue(null);
                name.setItems();
                name.setEnabled(false);
            } else {
                name.setItems(registry.tagsForNamespace(event.getValue()));
                name.setEnabled(true);
            }
            value.setValue(value.getEmptyValue());
            value.setEnabled(false);
        });
        name.setEnabled(false);
        name.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                value.setValue(value.getEmptyValue());
                value.setEnabled(false);
            } else {
                Optional<TagType<?>> type = registry.find(namespace.getValue(), name.getValue());
                type.ifPresent(o -> value.setEnabled(o.canHaveValue()));
            }
        });
        value.setEnabled(false);
        FormLayout form = new FormLayout(namespace, name, value);
        return form;
    }

    @Override
    public Tag formCompleted(Operation operation, Tag entity) {
        switch (operation) {
            case UPDATE:
                break;
            case CREATE:
                break;
            case DELETE:
                break;
        }
        return null;
    }

    @Override
    public void entityAdded(DataProvider<Tag, ?> dataProvider, Tag tag) {
        hasTags.add(tag);

    }

    @Override
    public void entityDeleted(DataProvider<Tag, ?> dataProvider, Tag tag) {
        hasTags.remove(tag);
    }
}
