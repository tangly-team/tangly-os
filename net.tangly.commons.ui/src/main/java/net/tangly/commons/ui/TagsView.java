/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.commons.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.commons.models.HasTags;
import net.tangly.commons.models.Tag;
import net.tangly.commons.models.TagType;
import net.tangly.commons.models.TagTypeRegistry;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * The view provides a view on the tags defined in the entity with tags. The following operations are provided - if the view is in read-only mode you
 * can only view the selectedItem values.
 */
public class TagsView extends GridFormView<Tag> {
    private TagTypeRegistry registry;
    private HasTags entity;
    private ComboBox<String> namespace;
    private ComboBox<String> name;
    private TextField value;

    public TagsView(TagTypeRegistry registry, HasTags entity) {
        super(false, "Filter by name...");
        this.registry = registry;
        this.entity = entity;
        namespace.setItems(registry.namespaces());
        updateFilteredItems(null);
    }

    @Override
    protected Collection<Tag> items() {
        return entity.tags();
    }

    @Override
    protected BiPredicate<Tag, String> filter() {
        return (o, text) -> o.name().startsWith(text);
    }

    @Override
    protected Grid<Tag> createGrid() {
        Grid<Tag> grid = new Grid<>();
        grid.addColumn(Tag::namespace).setHeader("Namespace").setSortable(true);
        grid.addColumn(Tag::name).setHeader("Name").setSortable(true);
        grid.addColumn(Tag::value).setHeader("Value");
        return grid;
    }

    @Override
    protected Tag saveItem() {
        Tag updatedItem = Tag.of(namespace.getValue(), name.getValue(), value.getValue());
        entity.replace(updatedItem);
        return updatedItem;
    }

    @Override
    protected void detailItem() {
    }

    @Override
    protected Tag deleteItem() {
        entity.remove(selectedItem);
        return selectedItem;
    }

    @Override
    protected void addItem() {
        namespace.setEnabled(true);
        name.setEnabled(false);
        value.setEnabled(false);
    }

    @Override
    protected FormLayout createForm() {
        FormLayout form = new FormLayout();
        namespace = new ComboBox("Namespace");
        namespace.setEnabled(false);
        namespace.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
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
        name = new ComboBox<>("Name");
        name.setEnabled(false);
        name.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                value.setValue(value.getEmptyValue());
                value.setEnabled(false);
            } else {
                Optional<TagType> type = registry.find(namespace.getValue(), name.getValue());
                type.ifPresent(o -> value.setEnabled(o.canHaveValue()));
            }
        });
        value = new TextField("Value");
        value.setEnabled(false);
        form.add(namespace, name, value, createButtons());
        return form;
    }

    @Override
    protected boolean isEditable(Tag item) {
        Optional<TagType> type = registry.find(item.namespace(), item.name());
        return type.isPresent() ? type.get().canHaveValue() : true;
    }

    @Override
    protected void selectItemDetails(Tag item) {
        if (item == null) {
            namespace.setValue(null);
        } else {
            Optional<TagType> type = registry.find(item);
            namespace.setValue((item == null) ? "" : item.namespace());
            namespace.setEnabled(false);
            name.setItems(registry.tagsForNamespace(item.namespace()));
            name.setValue((item == null) ? "" : item.name());
            name.setEnabled(false);
            if (type.isPresent() && type.get().canHaveValue()) {
                value.setEnabled(true);
                value.setValue((item == null) ? "" : item.value());
            } else {
                value.setEnabled(false);
            }
        }
    }
}

