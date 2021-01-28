/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.components.grids;

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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.commons.vaadin.CodeField;
import net.tangly.commons.vaadin.Crud;
import net.tangly.commons.vaadin.SelectedItemListener;
import net.tangly.core.HasDate;
import net.tangly.core.HasInterval;
import net.tangly.core.HasTags;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the filters and actions specific to a grid.
 *
 * @param <T> type of entities displayed in the grid
 */
public class GridDecorators<T> extends HorizontalLayout implements SelectedItemListener<T> {
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

    private final PaginatedGrid<T> grid;
    private final List<GridFilter<T>> filters;
    private final MenuBar menuBar;
    private final Actions itemActions;
    private final Actions globalActions;
    private T selectedItem;

    public GridDecorators(@NotNull PaginatedGrid<T> grid, boolean hasItemActions, boolean hasGlobalActions) {
        this.grid = grid;
        menuBar = new MenuBar();
        itemActions = hasItemActions ? new Actions(menuBar, "Actions") : null;
        globalActions = hasGlobalActions ? new Actions(menuBar, "Operations") : null;
        filters = new ArrayList<>();
        selectedItem(null);
        if (hasItemActions || hasGlobalActions) {
            add(menuBar);
        }
    }

    public static <U> GridDecorators<U> of(@NotNull Crud<U> view, @NotNull PaginatedGrid<U> grid, boolean hasItemActions, boolean hasGlobalActions) {
        GridDecorators<U> decorator = new GridDecorators<>(grid, hasItemActions, hasGlobalActions);
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
        if (Objects.nonNull(itemActions)) {
            itemActions.enabled(entity != null);
        }
    }

    public T selectedItem() {
        return selectedItem;
    }

    public GridDecorators<T> addFilter(@NotNull GridFilter<T> filter) {
        filters.add(filter);
        add(filter.component());
        return this;
    }

    void updateFilters() {
        dataProvider().clearFilters();
        filters.forEach(o -> o.addFilter(dataProvider()));
    }

    public interface GridFilter<E> {
        Component component();

        void addFilter(@NotNull ListDataProvider<E> provider);
    }

    public static class FilterTags<E extends HasTags> implements GridFilter<E> {
        private final TextField component;

        public FilterTags(@NotNull GridDecorators<E> container) {
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

    public static class FilterDate<E extends HasDate> implements GridFilter<E> {
        private final DatePicker from;
        private final DatePicker to;
        private final HorizontalLayout component;

        public FilterDate(@NotNull GridDecorators<E> container) {
            from = new DatePicker("From");
            from.setClearButtonVisible(true);
            from.addValueChangeListener(e -> container.updateFilters());
            to = new DatePicker("To");
            to.setClearButtonVisible(true);
            to.addValueChangeListener(e -> container.updateFilters());
            component = new HorizontalLayout();
            component.add(from, to);
        }

        public Component component() {
            return component;
        }

        public void addFilter(@NotNull ListDataProvider<E> provider) {
            if (!from.isEmpty() && !to.isEmpty()) {
                var predicate = new HasDate.IntervalFilter(from.getValue(), to.getValue());
                provider.addFilter(entity ->  predicate.test(entity));
            }
        }
    }

    public static class FilterInterval<E extends HasInterval> implements GridFilter<E> {
        private final DatePicker component;

        public FilterInterval(@NotNull GridDecorators<E> container) {
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

    public static class FilterText<E> implements GridFilter<E> {
        private final TextField component;
        private final Function<E, String> getter;

        public FilterText(@NotNull GridDecorators<E> container, @NotNull Function<E, String> getter, String label, String placeholder) {
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
                provider.addFilter(entity -> getter.apply(entity).toLowerCase().contains(component.getValue().toLowerCase()));
            }
        }
    }

    public static class FilterNumber<E, T> implements GridFilter<E> {
        private final NumberField component;
        private final Function<E, T> getter;

        public FilterNumber(@NotNull GridDecorators<E> container, @NotNull Function<E, T> getter, @NotNull String label, String placeholder) {
            component = new NumberField (label, placeholder);
            component.setClearButtonVisible(true);
            component.addValueChangeListener(e -> container.updateFilters());
            this.getter = getter;
        }

        public Component component() {
            return component;
        }

        public void addFilter(@NotNull ListDataProvider<E> provider) {
            if (!component.isEmpty()) {
                provider.addFilter(entity -> getter.apply(entity).equals(component.getValue()));
            }
        }
    }

    public static class FilterCode<E, C extends Code> implements GridFilter<E> {
        private final CodeField<C> component;
        private final Function<E, C> getter;

        public FilterCode(@NotNull GridDecorators<E> container, @NotNull CodeType<C> type, @NotNull Function<E, C> getter, String label) {
            component = new CodeField<C>(type, label);
            component.setEmptySelectionAllowed(true);
            component.addValueChangeListener(e -> container.updateFilters());
            this.getter = getter;
        }

        public Component component() {
            return component;
        }

        public void addFilter(@NotNull ListDataProvider<E> provider) {
            if (!component.isEmpty()) {
                provider.addFilter(entity -> getter.apply(entity).equals(component.getValue()));
            }
        }
    }

    private ListDataProvider<T> dataProvider() {
        return (ListDataProvider<T>) grid.dataProvider();
    }
}
