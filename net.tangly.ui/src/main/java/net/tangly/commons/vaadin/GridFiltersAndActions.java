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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.core.HasInterval;
import net.tangly.core.HasTags;
import org.jetbrains.annotations.NotNull;
import org.vaadin.klaudeta.PaginatedGrid;

public class GridFiltersAndActions<T> extends HorizontalLayout implements SelectedItemListener<T> {
    /**
     * Defines the structure for a submenu of context actions associated with entities displayed in a grid.
     */
    public static class Actions {
        private final MenuItem menuItem;
        private final SubMenu subMenu;

        public Actions(@NotNull MenuBar menuBar, @NotNull String label) {
            menuItem = menuBar.addItem(label);
            subMenu = menuItem.getSubMenu();
        }

        public void addAction(@NotNull String text, @NotNull ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
            subMenu.addItem(text, clickListener);
        }

        public void enabled(boolean enabled) {
            menuItem.setEnabled(enabled);
        }
    }

    private final ListDataProvider<T> provider;
    private final PaginatedGrid<T> grid;
    private final List<GridFilter<T>> filters;
    private final MenuBar menuBar;
    private final Actions itemActions;
    private final Actions globalActions;
    private T selectedItem;

    public GridFiltersAndActions(@NotNull PaginatedGrid<T> grid, boolean hasItemActions, boolean hasGlobalActions) {
        this.grid = grid;
        this.provider = (ListDataProvider<T>) grid.getDataProvider();
        menuBar = new MenuBar();
        itemActions = hasItemActions ? new Actions(menuBar, "Actions") : null;
        globalActions = hasGlobalActions ? new Actions(menuBar, "Operations") : null;
        filters = new ArrayList<>();
        selectedItem(null);
        if (hasItemActions || hasGlobalActions) {
            add(menuBar);
        }
    }

    public static <U> GridFiltersAndActions<U> of(@NotNull Crud<U> view, @NotNull PaginatedGrid<U> grid, boolean hasItemActions, boolean hasGlobalActions) {
        GridFiltersAndActions<U> decorator = new GridFiltersAndActions<>(grid, hasItemActions, hasGlobalActions);
        view.addSelectedItemListener(decorator);
        return decorator;
    }

    public void addItemAction(@NotNull String text, @NotNull ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Objects.requireNonNull(itemActions).addAction(text, clickListener);
    }

    public void addGlobalAction(@NotNull String text, @NotNull ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Objects.requireNonNull(globalActions).addAction(text, clickListener);
    }

    public void selectedItem(T entity) {
        selectedItem = entity;
        if (itemActions != null) {
            itemActions.enabled(entity != null);
        }
    }

    public T selectedItem() {
        return selectedItem;
    }

    public void addFilter(@NotNull GridFilter<T> filter) {
        filters.add(filter);
        this.add(filter.component());
    }

    void updateFilters() {
        provider.clearFilters();
        filters.forEach(o -> o.addFilter(provider));
    }

    public interface GridFilter<E> {
        Component component();

        void addFilter(@NotNull ListDataProvider<E> provider);
    }

    public static class GridFilterTags<E extends HasTags> implements GridFilter<E> {
        private final TextField component;

        public GridFilterTags(@NotNull GridFiltersAndActions<E> container) {
            component = new TextField("Tags", "tags");
            component.setClearButtonVisible(true);
            component.addValueChangeListener(e -> container.updateFilters());
        }

        public Component component() {
            return component;
        }

        public void addFilter(@NotNull ListDataProvider<E> provider) {
            if (!component.isEmpty()) {
                provider.addFilter(entity -> entity.containsTag(component.getValue()));
            }
        }
    }

    public static class GridFilterInterval<E extends HasInterval> implements GridFilter<E> {
        private final DatePicker component;

        public GridFilterInterval(@NotNull GridFiltersAndActions<E> container) {
            component = new DatePicker("Date");
            component.setClearButtonVisible(true);
            component.addValueChangeListener(e -> container.updateFilters());
        }

        public Component component() {
            return component;
        }

        public void addFilter(@NotNull ListDataProvider<E> provider) {
            if (!component.isEmpty()) {
                provider.addFilter(entity -> entity.isActive(component.getValue()));
            }
        }
    }

    public static class GridFilterText<E> implements GridFilter<E> {
        private final TextField component;
        private final Function<E, String> getter;

        public GridFilterText(@NotNull GridFiltersAndActions<E> container, @NotNull Function<E, String> getter, String label, String placeholder) {
            component = new TextField(label, placeholder);
            component.setClearButtonVisible(true);
            component.addValueChangeListener(e -> container.updateFilters());
            this.getter = getter;
        }

        public Component component() {
            return component;
        }

        public void addFilter(@NotNull ListDataProvider<E> provider) {
            if (!component.isEmpty()) {
                provider.addFilter(entity -> getter.apply(entity).contains(component.getValue()));
            }
        }
    }
}
