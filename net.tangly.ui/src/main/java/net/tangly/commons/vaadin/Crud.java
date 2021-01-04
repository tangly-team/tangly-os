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

import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.components.grids.PaginatedGrid;
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
public class Crud<T> extends VerticalLayout implements SelectedItemListener<T> {
    /**
     * Define the different edition modes of the CRUD component. The mode has an impact on the displayed fields and the buttons.
     * <dl>
     *     <dt>editable</dt><dd>The items of the list can be added to the list, deleted from the list, and properties of the item can be modified.
     *     Edition can be canceled.</dd>
     *     <dt>immutable</dt><dd>The items of the list are immutable. Items can be added or removed from the list.</dd>
     *     <dt>auditable</dt><dd>the items of the list are auditable therefore they are immutable. Items can only be added to the list.</dd>
     *     <dt>readonly</dt><dd>The items of the list and the list are readonly and immutable.</dd>
     *     <dt>edit_delete</dt><dd>The items of the list can be deleted from the list, and properties of the item can be modified. Edition can
     *     be cancelled.</dd>
     * </dl>
     */
    public enum Mode {
        EDITABLE, IMMUTABLE, AUDITABLE, READONLY, EDIT_DELETE;

        public boolean readOnly() {
            return (this == READONLY) || (this == AUDITABLE) || (this == EDIT_DELETE);
        }

        public boolean canUpdate() {
            return (this == EDITABLE) || (this == EDIT_DELETE);
        }

        public boolean canAdd() {
            return (this == EDITABLE) || (this == IMMUTABLE) || (this == AUDITABLE);
        }

        public boolean canDelete() {
            return (this == EDITABLE) || (this == IMMUTABLE) || (this == EDIT_DELETE);
        }

        public boolean isCancellable() {
            return (this != EDITABLE) && (this != EDIT_DELETE);
        }
    }

    private final Class<T> entityClass;
    private final PaginatedGrid<T> grid;
    private final Set<SelectedItemListener<T>> selectedItemListenerListeners;
    private Mode mode;
    private T selectedItem;

    public Crud(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull DataProvider<T, ?> dataProvider) {
        this.entityClass = entityClass;
        this.grid = new PaginatedGrid<>();
        this.selectedItemListenerListeners = new HashSet<>();
        this.mode = mode;
        grid.dataProvider(dataProvider);
        init();
    }

    public Crud(@NotNull Class<T> entityClass) {
        this.entityClass = entityClass;
        this.grid = new PaginatedGrid<>();
        this.selectedItemListenerListeners = new HashSet<>();
        init();
    }

    public static <T> Crud<T> of(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull DataProvider<T, ?> dataProvider) {
        Crud<T> crud = new Crud(entityClass, mode, dataProvider);
        crud.mode(mode);
        crud.grid().dataProvider(dataProvider);
        return crud;
    }

    protected void init() {
        grid.setPageSize(10);
        grid.paginatorSize(3);
        grid.asSingleSelect().addValueChangeListener(event -> selectedItem(event.getValue()));
        VaadinUtils.initialize(grid());
        grid.setMinHeight("5em");
        grid.setWidthFull();
        setSizeFull();
    }

    @Override
    public T selectedItem() {
        return selectedItem;
    }

    @Override
    public void selectedItem(T item) {
        selectedItemListenerListeners.forEach(o -> o.selectedItem(item));
        selectedItem = item;
    }

    public void addSelectedItemListener(SelectedItemListener<T> listener) {
        selectedItemListenerListeners.add(listener);
    }

    public Mode mode() {
        return mode;
    }

    public void mode(Mode mode) {
        this.mode = mode;
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

    protected Class<T> entityClass() {
        return entityClass;
    }

    protected PaginatedGrid<T> grid() {
        return grid;
    }
}
