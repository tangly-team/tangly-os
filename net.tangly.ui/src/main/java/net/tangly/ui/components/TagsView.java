/*
 * Copyright 2006-2023 Marcel Baumann
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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.HasTags;
import net.tangly.core.Tag;
import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

/**
 * The tags view is a Crud view with all the tags defined for an entity. Edition functions are provided to add, delete, and view individual comments. Update function is not
 * supported because tags are immutable objects. Update is therefore a create operation.
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
public class TagsView extends ItemView<Tag> implements BindableComponent.HasBindValue<HasTags> {
    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String NAMESPACE_LABEL = "Namespace";
    private static final String NAME_LABEL = "Name";
    private static final String Value_LABEL = "Value";

    static class TagFilter extends ItemFilter<Tag> {
        private String namespace;
        private String name;
        private String value;

        public TagFilter() {
        }

        public void namespace(String namespace) {
            this.namespace = namespace;
            refresh();
        }

        public void name(String name) {
            this.name = name;
            refresh();
        }

        public void value(String value) {
            this.value = value;
            refresh();
        }

        @Override
        public boolean test(@NotNull Tag entity) {
            return matches(entity.namespace(), namespace) && matches(entity.name(), name) && matches(entity.value(), value);
        }
    }

    static class TagForm extends ItemForm<Tag, TagsView> {
        private final transient TypeRegistry registry;
        private Binder<Tag> itemBinder;
        private ComboBox<String> namespace;
        private ComboBox<String> name;
        private TextField value;

        public TagForm(@NotNull TagsView parent, @NotNull TypeRegistry registry) {
            super(parent);
            this.registry = registry;
            init();
        }

        protected void init() {
            FormLayout fieldsLayout = new FormLayout();
            namespace = new ComboBox<>(NAMESPACE);
            name = new ComboBox<>(NAME);
            name.setRequired(true);
            value = new TextField(VALUE);
            fieldsLayout.add(namespace, name, value);
            fieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
            form().add(fieldsLayout, createButtonBar());
            itemBinder = new Binder<>(Tag.class);
            itemBinder.forField(namespace).bind(Tag::namespace, null);
            itemBinder.forField(name).bind(Tag::name, null);
            itemBinder.forField(value).bind(Tag::value, null);
        }

        @Override
        public void mode(@NotNull Mode mode) {
            namespace.setReadOnly(mode.readonly());
            name.setReadOnly(mode.readonly());
            value.setReadOnly(mode.readonly());
        }

        @Override
        public void value(Tag value) {
            namespace.addValueChangeListener(event -> {
                name.clear();
                if (Objects.nonNull(event.getValue())) {
                    name.setItems(registry.tagNamesForNamespace(event.getValue()));
                    name.setReadOnly(Objects.nonNull(value));
                    deactivateValue();
                }
            });
            name.addValueChangeListener(event -> {
                if (Objects.nonNull(event.getValue())) {
                    activateValue(registry.find(namespace.getValue(), name.getValue()).orElseThrow());
                }
            });
            if (Objects.nonNull(value)) {
                name.setItems(registry.tagNamesForNamespace(value.namespace()));
                itemBinder.readBean(value);
            } else {
                clear();
            }
        }

        @Override
        public void clear() {
            namespace.clear();
            name.clear();
            value.clear();
        }

        @Override
        protected Tag createOrUpdateInstance(Tag entity) {
            return new Tag(namespace.getValue(), name.getValue(), value.getValue());

        }

        private void deactivateValue() {
            value.clear();
            value.setEnabled(false);
            value.setReadOnly(true);
            value.setRequired(false);
        }

        private void activateValue(@NotNull TagType<?> type) {
            if (type.canHaveValue()) {
                value.setEnabled(true);
            } else {
                value.clear();
            }
            value.setRequired(type.mustHaveValue());
        }
    }

    private final transient TypeRegistry registry;
    private transient HasTags hasTags;

    /**
     * The view is created with an optional entity providing the tags to display. The tags set can be later updated by calling {@link ItemView#provider(Provider)}.
     *
     * @param entity   optional entity with the tags set
     * @param registry tag registry providing the tag type definitions
     */
    public TagsView(HasTags entity, @NotNull TypeRegistry registry, Mode mode) {
        super(Tag.class, null, ProviderInMemory.of(Objects.nonNull(entity) ? entity.tags() : Collections.emptySet()), new TagsView.TagFilter(), mode);
        this.hasTags = entity;
        this.registry = registry;
        init();
    }

    @Override
    public HasTags value() {
        return hasTags;
    }

    @Override
    public void value(HasTags value) {
        if (!Objects.equals(hasTags, value)) {
            this.hasTags = value;
            provider(ProviderInMemory.of(Objects.nonNull(value) ? value.tags() : Collections.emptySet()));
            dataView().refreshAll();
        }
    }

    @Override
    public void bind(@NotNull Binder<HasTags> binder, boolean readonly) {
    }

    @Override
    protected void init() {
        setHeight("15em");
        Grid<Tag> grid = grid();
        grid.addColumn(Tag::namespace).setKey(NAMESPACE).setHeader(NAMESPACE_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Tag::name).setKey(NAME).setHeader(NAME_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("20em");
        grid.addColumn(Tag::value).setKey(VALUE).setHeader(Value_LABEL).setSortable(false).setResizable(true).setFlexGrow(0).setWidth("25em");

        if (filter() instanceof TagFilter filter) {
            grid().getHeaderRows().clear();
            HeaderRow headerRow = grid().appendHeaderRow();
            addFilterText(headerRow, NAMESPACE, NAMESPACE_LABEL, filter::namespace);
            addFilterText(headerRow, NAME, NAMESPACE_LABEL, filter::name);
            addFilterText(headerRow, VALUE, Value_LABEL, filter::value);
        }
        buildMenu();
    }
}
