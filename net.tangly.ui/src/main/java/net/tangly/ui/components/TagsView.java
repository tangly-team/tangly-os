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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.core.Tag;
import net.tangly.core.TagType;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.app.domain.BoundedDomainUi;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The tags view is a Crud view with all the tags defined for an entity. Edition functions are provided to add, delete, and view individual comments. Update function is not
 * supported because tags are immutable objects. Update is therefore a create operation.
 * <p>The form uses information stored in the tag registry to populate the namespace field, the name field and toggle the value field based on the tag type
 * definition. The operations are:</p>
 * <dl>
 *     <dt>view</dt><dd>The tag properties are displayed in read-only fields. The operation can be acknowledged.</dd>
 *     <dt>create</dt><dd>The tag properties view is displayed with empty values. Only the tag names for the selected namespace will be displayed. The
 *     value field is only editable if the tag type supports values for the tag. The operation can be accepted or canceled.</dd>
 *     <dt>update</dt><dd>Only the value of the tag can be edited. The namespace and name of the tag are read-only.The operation can be accepted or canceled.</dd>
 *     <dt>delete</dt><dd>The selected tag is removed.The operation can be accepted or canceled.</dd>
 * </dl>
 */
public class TagsView extends ItemView<Tag> {
    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String NAMESPACE_LABEL = "Namespace";
    private static final String NAME_LABEL = "Name";
    private static final String VALUE_LABEL = "Value";

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
        private ComboBox<String> namespace;
        private ComboBox<String> name;
        private TextField value;

        public TagForm(@NotNull TagsView parent) {
            super(parent);
            init();
        }

        public void readonly(boolean readonly) {
            namespace.setReadOnly(readonly);
            name.setReadOnly(readonly);
            value.setReadOnly(readonly);
        }

        /**
         * Set of namespaces is already defined and available for display. If the namespace is changed, the tag name and value are cleared. If a new tag name is selected, configure
         * the value field either as mandatory, optional, or disabled.
         *
         * @param value value to display in the form
         */
        @Override
        public void value(Tag value) {
            if (Objects.nonNull(value)) {
                name.setItems(view().registry().tagNamesForNamespace(value.namespace()));
            }
            super.value(value);
        }

        @Override
        public void clear() {
            namespace.clear();
            name.clear();
            value.clear();
            value.setRequired(false);
        }

        /**
         * Handles the edition or addition of an immutable tag. If the parameter is not null, it is removed from the list before the changed version is added.
         * The updated property values are retrieved from the form. The logic of the item form we inherited from takes care of the provider update to
         * synchronize the user interface grid.
         *
         * @param entity the entity to update or null if it is a created or duplicated instance
         * @return new tag of the entity
         */
        @Override
        protected Tag createOrUpdateInstance(Tag entity) {
            Tag tag = new Tag(namespace.getValue(), name.getValue(), value.getValue());
            view().provider().replace(entity, tag);
            return tag;
        }

        private void init() {
            FormLayout layout = new FormLayout();
            namespace = new ComboBox<>(NAMESPACE);
            namespace.setItems(view().registry().namespaces());
            namespace.setAllowCustomValue(false);
            namespace.addValueChangeListener(event -> {
                this.name.clear();
                deactivateValue();
                if (Objects.nonNull(event.getValue())) {
                    name.setItems(view().registry().tagNamesForNamespace(event.getValue()));
                }
            });
            name = new ComboBox<>(NAME);
            name.setRequired(true);
            name.setAllowCustomValue(false);
            name.addValueChangeListener(event -> {
                if (Objects.nonNull(event.getValue())) {
                    activateValue(view().registry().find(namespace.getValue(), name.getValue()).orElseThrow());
                }
            });
            value = new TextField(VALUE);
            layout.add(namespace, name, value);
            layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
            form().add(layout, createButtonBar());
            binder().forField(namespace).bind(Tag::namespace, null);
            binder().forField(name).bind(Tag::name, null);
            binder().forField(value).bind(Tag::value, null);
        }

        private void deactivateValue() {
            value.clear();
            value.setEnabled(false);
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

    public TagsView(@NotNull BoundedDomainUi<?> domain, @NotNull Mode mode) {
        super(Tag.class, domain, ProviderInMemory.of(), new TagFilter(), mode);
        form(() -> new TagForm(this));
        init();
    }

    private void init() {
        setHeight("15em");
        Grid<Tag> grid = grid();
        grid.addColumn(Tag::namespace).setKey(NAMESPACE).setHeader(NAMESPACE_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Tag::name).setKey(NAME).setHeader(NAME_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Tag::value).setKey(VALUE).setHeader(VALUE_LABEL).setSortable(false).setResizable(true).setFlexGrow(0).setWidth("10em");

        if (filter() instanceof TagFilter filter) {
            HeaderRow headerRow = createHeaderRow();
            headerRow.getCell(grid.getColumnByKey(NAMESPACE)).setComponent(createTextFilterField(filter::namespace));
            headerRow.getCell(grid.getColumnByKey(NAME)).setComponent(createTextFilterField(filter::name));
            headerRow.getCell(grid.getColumnByKey(VALUE)).setComponent(createTextFilterField(filter::value));
        }
    }
}
