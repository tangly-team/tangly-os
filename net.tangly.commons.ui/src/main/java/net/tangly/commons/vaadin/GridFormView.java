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

import java.util.Collection;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * The view provides the abstraction of a list of entities and a detailed form view of a selected entity. The view provides a view on the tags defined
 * in the entity with tags. The following operations are provided - if the view is in read-only mode you can only view the selectedItem values.
 * <ul>
 * <li>List of all tags defined for the entity owning the tags. The list can filtered by name.</li>
 * <li>Edit the value of a selectedItem. After editing the value you can either cancel the changes or save them. The type of selectedItem must allow
 * values to enable these operations.</li>
 * <li>Delete a seleted selectedItem.</li>
 * <li>Create a new selectedItem. After defining the namespace, name and if available the value, you can either cancel the changes or save them.</li>
 * </ul>
 *
 * @param <E> entity to display in the list and the details form
 */
public abstract class GridFormView<E> extends Composite<Div> {
    protected transient E selectedItem;
    private boolean hasDetails;
    private TextField filterText;
    private Grid<E> grid;
    private Button save = new Button("Save");
    private Button cancel = new Button("Cancel");
    private Button details = new Button("Details");
    private Button add = new Button("Add");
    private Button delete = new Button("Delete");


    protected GridFormView(boolean hasDetails, String filterPlaceholder) {
        this.hasDetails = hasDetails;
        grid = createGrid();
        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedItem = event.getValue();
            if (selectedItem == null) {
                unselectedItem();
            } else {
                selectedItem(isEditable(selectedItem));
            }
            selectItemDetails(selectedItem);
        });
        grid.setSizeFull();
        HorizontalLayout main = new HorizontalLayout(grid, createForm());
        main.setAlignItems(FlexComponent.Alignment.START);
        main.setSizeFull();
        getContent().setHeight("40vh");
        getContent().add(createGridFilter(filterPlaceholder), main);
    }

    protected HorizontalLayout createButtons() {
        save.addClickListener(event -> {
            saveItem();
            updateFilteredItems(saveItem());
        });
        cancel.addClickListener(event -> {
            cancelItem();
            selectItemDetails(selectedItem);
        });
        details.addClickListener(event -> detailItem());
        add.addClickListener(event -> {
            addedItem();
            addItem();
        });
        delete.addClickListener(event -> {
            deleteItem();
            updateFilteredItems(null);
        });
        HorizontalLayout group1 = new HorizontalLayout();
        group1.getStyle().set("marginRight", "100px");
        group1.add(save, cancel, details);
        details.setVisible(hasDetails);
        HorizontalLayout group2 = new HorizontalLayout();
        group2.add(add, delete);
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(group1, group2);
        actions.setVerticalComponentAlignment(FlexComponent.Alignment.START, group1);
        actions.setVerticalComponentAlignment(FlexComponent.Alignment.END, group2);
        return actions;
    }

    protected HorizontalLayout createGridFilter(String placeholder) {
        filterText = new TextField();
        filterText.setPlaceholder(placeholder);
        filterText.setValueChangeMode(ValueChangeMode.EAGER);
        filterText.addValueChangeListener(e -> updateFilteredItems(selectedItem));
        Button clearFilterTextBtn = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE));
        clearFilterTextBtn.addClickListener(e -> filterText.clear());
        return new HorizontalLayout(filterText, clearFilterTextBtn);
    }

    protected void unselectedItem() {
        save.setEnabled(false);
        cancel.setEnabled(false);
        details.setEnabled(false);
        delete.setEnabled(false);
        add.setEnabled(true);
    }

    protected void selectedItem(boolean editable) {
        save.setEnabled(editable);
        cancel.setEnabled(editable);
        details.setEnabled(true);
        delete.setEnabled(true);
        add.setEnabled(true);
    }

    protected void addedItem() {
        grid.select(null);
        save.setEnabled(true);
        cancel.setEnabled(true);
        details.setEnabled(false);
        delete.setEnabled(false);
        add.setEnabled(false);
    }

    protected void updateFilteredItems(E selectedItem) {
        String value = filterText.getValue();
        Set<E> filteredItems = items().stream().filter(o -> filter().test(o, value)).collect(Collectors.toSet());
        grid.setItems(filteredItems);
        if (filteredItems.contains(selectedItem)) {
            grid.select(selectedItem);
            selectItemDetails(selectedItem);
        } else {
            selectItemDetails(null);
        }
    }

    protected boolean isEditable(E item) {
        return true;
    }

    protected abstract E saveItem();

    protected void cancelItem() {
        selectItemDetails(selectedItem);
    }

    protected abstract void detailItem();

    protected abstract E deleteItem();

    protected abstract void addItem();

    protected abstract void selectItemDetails(E item);

    protected abstract Grid<E> createGrid();

    protected abstract FormLayout createForm();

    protected abstract Collection<E> items();

    protected abstract BiPredicate<E, String> filter();
}