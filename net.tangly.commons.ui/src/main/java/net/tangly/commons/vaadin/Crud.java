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

import java.util.function.Consumer;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import org.jetbrains.annotations.NotNull;

/**
 * <p>The CRUD provides a grid view to a set of entities and views to view, update, create and delete an entity. The core view is the same for all
 * operations. Tailoring is supported to determine if a property is visible, read-only, or editable. The view operation can be either confirmed or canceled. The
 * operations semantics require single select operation on the list of entities.</p>
 * <p>The configuration of the components has two dimensions.</p>
 * <ul>
 *     <li>The columns of the grid and their behavior and how a cell containing a property value is displayed.</li>
 *     <li>The form use to perform the CRUD operation on the selected entity in the grid.</li>
 * </ul>
 * <p>The component handles all buttons of the grid component and of the forms.</p>
 *
 * @param <T> represents the entity displayed and manipulated in the CRUD component.
 */
public class Crud<T> extends VerticalLayout {
    /**
     * Define the different edition modes of the CRUD component.
     */
    public enum Mode {
        EDITABLE, RECORD_EDITABLE, IMMUTABLE, AUDITABLE, READONLY;

        static boolean canUpdate(@NotNull Mode mode) {
            return (mode == EDITABLE);
        }

        static boolean canAdd(@NotNull Mode mode) {
            return (mode == EDITABLE) || (mode == IMMUTABLE) || (mode == AUDITABLE);
        }

        static boolean canDelete(@NotNull Mode mode) {
            return (mode == EDITABLE) || (mode == IMMUTABLE);
        }
    }

    private final Class<T> entityClass;
    private final Mode mode;
    private final Grid<T> grid;
    private T selectedItem;
    private CrudForm<T> form;
    private CrudActionsListener<T> actionsListener;

    private Button details;
    private Button add;
    private Button update;
    private Button delete;

    public Crud(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull Consumer<Grid<T>> gridConfigurator, @NotNull DataProvider<T, ?> dataProvider) {
        this.entityClass = entityClass;
        this.mode = mode;
        this.grid = new Grid<>(entityClass, false);

        grid.setDataProvider(dataProvider);
        grid.asSingleSelect().addValueChangeListener(event -> selectItem(event.getValue()));
        grid.setMinHeight("20em");
        grid.setWidthFull();
        gridConfigurator.accept(grid);

        setSizeFull();
        addAndExpand(grid, createCrudButtons());
        selectItem(null);
    }

    public void refreshData() {
        grid.getDataProvider().refreshAll();
    }

    public T selectedItem() {
        return selectedItem;
    }

    public void selectedItem(T selectedItem) {
        this.selectedItem = selectedItem;
    }

    protected void initialize(@NotNull CrudForm<T> form, @NotNull CrudActionsListener<T> actionsListener) {
        this.form = form;
        this.actionsListener = actionsListener;
    }

    protected Class<T> entityClass() {
        return entityClass;
    }

    protected Grid<T> grid() {
        return grid;
    }

    private HorizontalLayout createCrudButtons() {
        details = new Button("Details", VaadinIcon.ELLIPSIS_H.create(), event -> displayDialog(CrudForm.Operation.VIEW));
        add = new Button("Add", VaadinIcon.PLUS.create(), event -> displayDialog(CrudForm.Operation.CREATE));
        update = new Button("Update", VaadinIcon.PENCIL.create(), event -> displayDialog(CrudForm.Operation.UPDATE));
        delete = new Button("Delete", VaadinIcon.TRASH.create(), event -> displayDialog(CrudForm.Operation.DELETE));

        add.setEnabled((mode == Mode.EDITABLE) || (mode == Mode.IMMUTABLE) || (mode == Mode.AUDITABLE));
        update.setEnabled(mode == Mode.EDITABLE);
        delete.setEnabled((mode == Mode.EDITABLE) || (mode == Mode.IMMUTABLE));

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(add, delete, update, details);
        return actions;
    }

    private void displayDialog(CrudForm.Operation operation) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setResizable(true);
        dialog.add(new VerticalLayout(form.createForm(operation, operation != CrudForm.Operation.CREATE ? selectedItem : null), new HtmlComponent("br"),
                CrudForm.createFormButtons(dialog, form, operation, selectedItem(), actionsListener)));
        dialog.open();
    }

    protected void selectItem(T item) {
        selectedItem = item;
        if (item != null) {
            details.setEnabled(true);
            add.setEnabled(Mode.canAdd(mode));
            update.setEnabled(Mode.canUpdate(mode));
            delete.setEnabled(Mode.canDelete(mode));
        } else {
            details.setEnabled(false);
            add.setEnabled(Mode.canAdd(mode));
            update.setEnabled(false);
            delete.setEnabled(false);
        }
    }
}
