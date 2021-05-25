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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.ui.grids.PaginatedGrid;
import net.tangly.core.HasTags;
import net.tangly.core.Tag;
import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The tags view is a Crud view with all the tags defined for an entity. Edition functions are provided to add, delete, and view individual comments. Update
 * function is not supported because tags are immutable objects. Update is therefore a create operation.
 * <p>The form uses information stored in the tag registry to populate the namespace field, the name field and toggle the value field based on the tag type
 * definition. The operations are:</p>
 * <dl>
 *     <dt>view</dt><dd>The tag properties are displayed in read-only fields. The operation can be acknowledged.</dd>
 *     <dt>create</dt><dd>The tag properties view is displayed with empty values. Only the tag names for the selected namespace will be displayed. The
 *     value field is only editable if the tag type supports values for the tag. The operation can be accepeted or canceled.</dd>
 *     <dt>update</dt><dd>Only the value of the tag can be edited. The namespace and name of the tag are read-only.The operation can be accepeted or canceled.</dd>
 *     <dt>delete</dt><dd>The selected tag is removed.The operation can be accepeted or canceled.</dd>
 * </dl>
 */
public class TagsView extends EntitiesView<Tag> {
    private final transient HasTags hasTags;
    private final transient TypeRegistry registry;
    private final transient ComboBox<String> namespace;
    private final transient ComboBox<String> name;
    private final transient TextField value;
    private final transient Binder<Tag> binder;

    public TagsView(@NotNull Mode mode, @NotNull HasTags entity, @NotNull TypeRegistry registry) {
        super(Tag.class, mode, ProviderInMemory.of(entity.tags()));
        this.hasTags = entity;
        this.registry = registry;
        namespace = new ComboBox<>("Namespace");
        namespace.setItems(registry.namespaces());
        name = new ComboBox<>("Name");
        name.setRequired(true);
        value = new TextField("Value", "value");
        binder = new Binder<>(Tag.class);
        binder.bind(namespace, Tag::namespace, null);
        binder.bind(name, Tag::name, null);
        binder.bind(value, Tag::value, null);

        initialize();
    }

    @Override
    protected void initialize() {
        PaginatedGrid<Tag> grid = grid();
        grid.addColumn(Tag::namespace).setKey("namespace").setHeader("Namespace").setSortable(true).setFlexGrow(0).setWidth("10em").setResizable(false);
        grid.addColumn(Tag::name).setKey("name").setHeader("Name").setSortable(true).setFlexGrow(0).setWidth("10em").setResizable(false);
        grid.addColumn(Tag::value).setKey("value").setHeader("Value").setSortable(false).setFlexGrow(0).setWidth("20em").setResizable(false);
        addAndExpand(grid(), gridButtons());
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Tag entity, @NotNull FormLayout form) {
        namespace.setReadOnly(Objects.nonNull(entity) || operation.isReadOnly());
        name.setReadOnly(true);
        deactivateValue();
        namespace.addValueChangeListener(event -> {
            name.clear();
            if (Objects.nonNull(event.getValue())) {
                name.setItems(registry.tagNamesForNamespace(event.getValue()));
                name.setReadOnly(Objects.nonNull(entity));
                deactivateValue();
            }
        });
        name.addValueChangeListener(event -> {
            if (Objects.nonNull(event.getValue())) {
                activateValue(registry.find(namespace.getValue(), name.getValue()).orElseThrow(), operation);
            }
        });
        if (Objects.nonNull(entity)) {
            name.setItems(registry.tagNamesForNamespace(entity.namespace()));
            binder.readBean(entity);
        } else {
            namespace.clear();
            name.clear();
            value.clear();
        }
        form.add(namespace, name);
        form.add(value, 2);
        VaadinUtils.set3ResponsiveSteps(form);
        return form;
    }

    @Override
    protected Tag updateOrCreate(Tag entity) {
        return new Tag(namespace.getValue(), name.getValue(), value.getValue());
    }

    @Override
    public Tag formCompleted(@NotNull Operation operation, Tag entity) {
        return switch (operation) {
            case CREATE -> {
                Tag tag = updateOrCreate(entity);
                hasTags.add(tag);
                yield tag;
            }
            case UPDATE -> {
                Tag tag = updateOrCreate(entity);
                hasTags.update(tag);
                yield tag;
            }
            case DELETE -> {
                hasTags.remove(entity);
                yield entity;
            }
            default -> entity;
        };
    }

    private void deactivateValue() {
        value.clear();
        value.setEnabled(false);
        value.setReadOnly(true);
        value.setRequired(false);
    }

    private void activateValue(@NotNull TagType<?> type, @NotNull Operation operation) {
        if (type.canHaveValue()) {
            value.setEnabled(true);
        } else {
            value.clear();
        }
        value.setReadOnly(operation.isReadOnly());
        value.setRequired(type.mustHaveValue());
    }
}
