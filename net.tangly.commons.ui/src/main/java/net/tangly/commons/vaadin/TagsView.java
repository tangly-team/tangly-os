/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.vaadin;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.Tag;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The tags view is a Crud view with all the tags defined for an entity. Edition functions are provided to add, delete, and view individual comments. Update
 * function is not supported because tags are immutable objects.
 * <p>The form uses information stored in the tag registry to populate the namespace field, the name field and toggle the value field based on the tag type
 * definition.</p>
 */
public class TagsView extends Crud<Tag> implements CrudForm<Tag> {
    private final transient HasTags hasTags;
    private final transient TagTypeRegistry registry;
    private final transient ComboBox<String> namespace;
    private final transient ComboBox<String> name;
    private final transient TextField value;

    public TagsView(@NotNull Mode mode, @NotNull HasTags entity, @NotNull TagTypeRegistry registry) {
        super(Tag.class,mode, TagsView::defineGrid, new ListDataProvider<>(entity.tags()));
        initialize(this, new GridActionsListener<>(grid().getDataProvider(), this::selectedItem));
        this.hasTags = entity;
        this.registry = registry;
        namespace = new ComboBox<>("Namespace");
        name = new ComboBox<>("Name");
        value = new TextField("Value");
    }

    public static void defineGrid(@NotNull Grid<Tag> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Tag::namespace).setKey("namespace").setHeader("Namespace").setSortable(true).setFlexGrow(0).setWidth("10em").setResizable(false);
        grid.addColumn(Tag::name).setKey("name").setHeader("Name").setSortable(true).setFlexGrow(0).setWidth("10em").setResizable(false);
        grid.addColumn(Tag::value).setKey("value").setHeader("Value").setSortable(false).setFlexGrow(0).setWidth("20em").setResizable(false);
    }

    @Override
    public FormLayout createForm(@NotNull Operation operation, Tag entity) {
        boolean readonly = Operation.isReadOnly(operation);
        namespace.setItems(registry.namespaces());
        if (entity != null) {
            namespace.setValue(entity.namespace());
        }
        namespace.setReadOnly(readonly);
        namespace.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                name.clear();
            } else {
                name.setItems(registry.tagNamesForNamespace(event.getValue()));
            }
        });
        name.setReadOnly(readonly);
        if (entity != null) {
            name.setItems(registry.tagNamesForNamespace(entity.namespace()));
        }
        name.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                name.clear();
                value.setEnabled(true);
            } else {
                registry.find(namespace.getValue(), name.getValue()).ifPresent(o -> value.setEnabled(o.canHaveValue()));
            }
        });
        value.setPlaceholder("value");
        value.setReadOnly(readonly);

        if (readonly) {
            Binder<Tag> binder = new Binder<>(Tag.class);
            binder.bind(namespace, Tag::namespace, null);
            binder.bind(name, Tag::name, null);
            binder.bind(value, Tag::value, null);
            binder.readBean(entity);
        } else {
            value.clear();
        }
        FormLayout form = new FormLayout(namespace, name);
        form.add(value, 2);
        VaadinUtils.setResponsiveSteps(form);
        return form;
    }

    @Override
    public Tag formCompleted(@NotNull Operation operation, Tag entity) {
        return switch (operation) {
            case CREATE -> {
                Tag tag = create();
                hasTags.add(tag);
                yield tag;
            }
            case DELETE -> {
                hasTags.remove(entity);
                yield entity;
            }
            default -> entity;
        };
    }

    private Tag create() {
        return new Tag(namespace.getValue(), name.getValue(), value.getValue());
    }
}
