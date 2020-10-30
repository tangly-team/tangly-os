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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import org.jetbrains.annotations.NotNull;
import org.vaadin.klaudeta.PaginatedGrid;

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
     * Define the different edition modes of the CRUD component. The mode has an impact on the displayed fields and the buttons.
     */
    public enum Mode {
        EDITABLE, RECORD_EDITABLE, IMMUTABLE, AUDITABLE, READONLY;

        public static boolean readOnly(@NotNull Mode mode) {
            return (mode == READONLY) || (mode == AUDITABLE);
        }

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
    private final PaginatedGrid<T> grid;
    private T selectedItem;
    private CrudForm<T> form;
    private CrudActionsListener<T> actionsListener;

    private Button details;
    private Button add;
    private Button update;
    private Button delete;

    public Crud(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull DataProvider<T, ?> dataProvider) {
        this.entityClass = entityClass;
        this.mode = mode;

        this.grid = new PaginatedGrid<>(entityClass);
        grid.setPageSize(10);
        grid.setPaginatorSize(3);

        grid.setDataProvider(dataProvider);
        grid.asSingleSelect().addValueChangeListener(event -> selectedItem(event.getValue()));
        VaadinUtils.initialize(grid());
        grid.setMinHeight("5em");
        grid.setWidthFull();
        setSizeFull();

        details = new Button("Details", VaadinIcon.ELLIPSIS_H.create(), event -> displayDialog(CrudForm.Operation.VIEW));
        add = new Button("Add", VaadinIcon.PLUS.create(), event -> displayDialog(CrudForm.Operation.CREATE));
        update = new Button("Update", VaadinIcon.PENCIL.create(), event -> displayDialog(CrudForm.Operation.UPDATE));
        delete = new Button("Delete", VaadinIcon.TRASH.create(), event -> displayDialog(CrudForm.Operation.DELETE));

        add.setEnabled((mode == Mode.EDITABLE) || (mode == Mode.IMMUTABLE) || (mode == Mode.AUDITABLE));
        update.setEnabled(mode == Mode.EDITABLE);
        delete.setEnabled((mode == Mode.EDITABLE) || (mode == Mode.IMMUTABLE));

    }

    protected void initialize(@NotNull CrudForm<T> form, @NotNull CrudActionsListener<T> actionsListener) {
        this.form = form;
        this.actionsListener = actionsListener;
        selectedItem(null);
    }

    /**
     * Maps a form operation to a compatible mode for the view. This mapping is needed when a view is displayed inside a form.
     *
     * @param operation form operation requested by user
     * @return mode of the view compatible with the form operation
     */
    public static Mode of(CrudForm.Operation operation) {
        return switch (operation) {
            case VIEW, DELETE, CANCEL, SELECT -> Mode.READONLY;
            case UPDATE, CREATE -> Mode.EDITABLE;
        };
    }

    public void refreshData() {
        grid.getDataProvider().refreshAll();
    }

    /**
     * Return the selected item of the view
     *
     * @return selected item
     * @see #selectedItem(Object)
     */
    public T selectedItem() {
        return selectedItem;
    }

    /**
     * Select programmatically the item in the grid and update the grid and the state of the buttons associated with the view.
     * Is also called when an element is selected in the grid.
     *
     * @param item new selected item in the grid
     * @see #selectedItem()
     */
    public void selectedItem(T item) {
        selectedItem = item;
        if (selectedItem != null) {
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

    protected Class<T> entityClass() {
        return entityClass;
    }

    protected Grid<T> grid() {
        return grid;
    }

    protected HorizontalLayout createCrudButtons() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(add, delete, update, details);
        return actions;
    }

    private void displayDialog(CrudForm.Operation operation) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setWidth("90vw");
        dialog.setHeight("70vh");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.add(new VerticalLayout(form.createForm(operation, operation != CrudForm.Operation.CREATE ? selectedItem : null), new HtmlComponent("br"),
                CrudForm.createFormButtons(dialog, form, operation, selectedItem(), actionsListener)));
        dialog.open();
    }
}
