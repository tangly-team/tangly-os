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

import java.util.function.Consumer;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;

/**
 * <p>The CRUD provides a grid view to a set of entities and views to view, update, create and delete an entity. The core view is the same for all
 * operations. Tailoring is supported to determine if a property is visible, read-only, or editable. The view operation can be either confirmed or
 * canceled. The operations semantics require single select operation on the list of entities.</p>
 * <p>The configuration of the components has two dimensions.</p>
 * <ul>
 *     <li>The columns of the grid and their behavior and how a cell containing a property value is displayed.</li>
 *     <li>The form use to perform the CRUD operation on the selected entity in the grid.</li>
 * </ul>
 * <p>The component handles all buttons of the grid component and of the forms.</p>
 *
 * @param <T> represents the entity displayed and manipulated in the CRUD component.
 */
public class Crud<T> extends Composite<Div> {

    private final Class<T> entityClass;
    private T selectedItem;
    private final boolean readOnly;
    private final Grid<T> grid;
    private final CrudForm<T> form;
    private final CrudActionsListener<T> actionsListener;

    private Button details;
    private Button add;
    private Button update;
    private Button delete;

    public Crud(Class<T> entityClass, boolean readOnly, Consumer<Grid> gridConfigurator, DataProvider<T, ?> dataProvider, CrudForm<T> form,
                CrudActionsListener listener) {
        this.entityClass = entityClass;
        this.readOnly = readOnly;
        this.grid = new Grid<>(entityClass, false);
        gridConfigurator.accept(grid);
        grid.setDataProvider(dataProvider);
        this.form = form;
        this.actionsListener = listener;
        grid.asSingleSelect().addValueChangeListener(event -> selectItem(event.getValue()));
        grid.setSizeFull();
        getContent().add(grid, createCrudButtons());
        getContent().setSizeFull();
        getContent().setWidth("80vw");
        getContent().setHeight("40vh");
        selectItem(null);
    }

    private HorizontalLayout createCrudButtons() {
        details = new Button("Details", VaadinIcon.ELLIPSIS_H.create(), event -> displayDialog(CrudForm.Operation.VIEW));
        add = new Button("Add", VaadinIcon.PLUS.create(), event -> displayDialog(CrudForm.Operation.CREATE));
        update = new Button("Update", VaadinIcon.PENCIL.create(), event -> displayDialog(CrudForm.Operation.UPDATE));
        delete = new Button("Delete", VaadinIcon.TRASH.create(), event -> displayDialog(CrudForm.Operation.DELETE));

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(add, delete, update, details);
        return actions;
    }

    private void displayDialog(CrudForm.Operation operation) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.add(form.createForm(operation, operation != CrudForm.Operation.CREATE ? selectedItem : null), createFormButtons(dialog, operation));
        dialog.open();
    }

    private HorizontalLayout createFormButtons(Dialog dialog, CrudForm.Operation operation) {
        HorizontalLayout actions = new HorizontalLayout();
        Button cancel = new Button("Cancel", event -> {
            form.formCancelled(null, selectedItem);
            dialog.close();
        });
        Button action;
        switch (operation) {
            case VIEW:
                action = new Button("Ok");
                actions.add(action);
                action.addClickListener(event -> {
                    form.formCompleted(CrudForm.Operation.VIEW, selectedItem);
                    dialog.close();
                });
                break;
            case UPDATE:
                action = new Button("Update");
                actions.add(action, cancel);
                action.addClickListener(event -> {
                    form.formCompleted(CrudForm.Operation.UPDATE, selectedItem);
                    actionsListener.entityUpdated(grid.getDataProvider(), selectedItem);
                    dialog.close();
                    grid.getDataProvider().refreshItem(selectedItem);
                });
                break;
            case CREATE:
                action = new Button("Create");
                actions.add(action, cancel);
                action.addClickListener(event -> {
                    T created = form.formCompleted(CrudForm.Operation.CREATE, null);
                    actionsListener.entityAdded(grid.getDataProvider(), created);
                    dialog.close();
                    selectItem(created);
                    grid.getDataProvider().refreshAll();
                });
                break;
            case DELETE:
                action = new Button("Delete");
                actions.add(action, cancel);
                action.addClickListener(event -> {
                    form.formCompleted(CrudForm.Operation.DELETE, selectedItem);
                    actionsListener.entityDeleted(grid.getDataProvider(), selectedItem);
                    dialog.close();
                    selectItem(null);
                    grid.getDataProvider().refreshAll();
                });
                break;
        }
        return actions;
    }

    private void selectItem(T item) {
        selectedItem = item;
        if (item != null) {
            details.setEnabled(true);
            add.setEnabled(!readOnly);
            update.setEnabled(!readOnly);
            delete.setEnabled(!readOnly);
        } else {
            details.setEnabled(false);
            add.setEnabled(!readOnly);
            update.setEnabled(false);
            delete.setEnabled(false);
        }
    }
}
