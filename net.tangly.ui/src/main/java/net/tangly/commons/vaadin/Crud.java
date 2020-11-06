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

import com.vaadin.flow.component.grid.Grid;
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
public class Crud<T> extends VerticalLayout implements SelectedItemListener<T> {
    /**
     * Define the different edition modes of the CRUD component. The mode has an impact on the displayed fields and the buttons.
     */
    public enum Mode {
        EDITABLE, IMMUTABLE, AUDITABLE, READONLY, EDIT_DELETE;

        public static boolean readOnly(@NotNull Mode mode) {
            return (mode == READONLY) || (mode == AUDITABLE);
        }

        static boolean canUpdate(@NotNull Mode mode) {
            return (mode == EDITABLE) || (mode == EDIT_DELETE);
        }

        static boolean canAdd(@NotNull Mode mode) {
            return (mode == EDITABLE) || (mode == IMMUTABLE) || (mode == AUDITABLE);
        }

        static boolean canDelete(@NotNull Mode mode) {
            return (mode == EDITABLE) || (mode == IMMUTABLE) || (mode == EDIT_DELETE);
        }
    }

    private final Class<T> entityClass;
    private final Mode mode;
    private final PaginatedGrid<T> grid;
    private final Set<SelectedItemListener<T>> selectedItemListenerListeners;
    private T selectedItem;

    public Crud(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull DataProvider<T, ?> dataProvider) {
        this.entityClass = entityClass;
        this.mode = mode;
        this.selectedItemListenerListeners = new HashSet<>();

        this.grid = new PaginatedGrid<>(entityClass);
        grid.setPageSize(10);
        grid.setPaginatorSize(3);

        grid.setDataProvider(dataProvider);
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
        this.selectedItemListenerListeners.add(listener);
    }

    public Mode mode() {
        return mode;
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

    protected Grid<T> grid() {
        return grid;
    }
}
