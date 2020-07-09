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
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.Tag;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The comments view is a Crud view with all the comments defined for an entity. Edition functions are provided to add, delete, and view individual
 * comments. Update function is not supported because comments are immutable objects.
 */
public class TagsView extends Crud<Tag> implements CrudForm<Tag>, CrudActionsListener<Tag> {
    private final transient HasTags hasItems;
    private final transient TagTypeRegistry registry;
    private ComboBox<String> namespace;
    private ComboBox<String> name;
    private TextField value;

    public TagsView(@NotNull HasTags entity, @NotNull TagTypeRegistry registry) {
        super(Tag.class, Mode.IMMUTABLE, TagsView::defineGrid, new ListDataProvider<>(entity.tags()));
        initialize(this, this);
        this.hasItems = entity;
        this.registry = registry;
    }

    public static void defineGrid(@NotNull Grid<Tag> grid) {
        initialize(grid);
        grid.addColumn(Tag::namespace).setKey("namespace").setHeader("Namespace").setSortable(true).setFlexGrow(0).setWidth("200px")
                .setResizable(false).setFrozen(true);
        grid.addColumn(Tag::name).setKey("name").setHeader("Name").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Tag::value).setKey("value").setHeader("Value").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
    }

    @Override
    public FormLayout createForm(Operation operation, Tag entity) {
        boolean readonly = Operation.isReadOnly(operation);

        namespace = new ComboBox<>("Namespace");
        namespace.setItems(registry.namespaces());
        if (entity != null) {
            namespace.setValue(entity.namespace());
        }
        namespace.setReadOnly(readonly);
        namespace.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                name.clear();
            } else {
                name.setItems(registry.tagsForNamespace(event.getValue()));
            }
        });

        name = new ComboBox<>("Name");
        if (entity != null) {
            name.setItems(registry.tagsForNamespace(entity.namespace()));
        }
        name.setReadOnly(readonly);

        value = new TextField("Value");
        value.setPlaceholder("value");
        value.setReadOnly(readonly);

        if (readonly) {
            Binder<Tag> binder = new Binder<>(Tag.class);
            binder.bind(namespace, Tag::namespace, null);
            binder.bind(name, Tag::name, null);
            binder.bind(value, Tag::value, null);
            binder.readBean(entity);
        }

        FormLayout form = new FormLayout(namespace, name, value);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("21em", 2),
                new FormLayout.ResponsiveStep("42em", 3)
        );
        return form;
    }

    @Override
    public Tag formCompleted(Operation operation, Tag entity) {
        switch (operation) {
            case CREATE:
                return new Tag(namespace.getValue(), name.getValue(), value.getValue());
            case DELETE:
                break;
        }
        return entity;
    }

    @Override
    public void entityAdded(DataProvider<Tag, ?> dataProvider, Tag entity) {
        hasItems.add(entity);

    }

    @Override
    public void entityDeleted(DataProvider<Tag, ?> dataProvider, Tag entity) {
        hasItems.remove(entity);
    }
}
