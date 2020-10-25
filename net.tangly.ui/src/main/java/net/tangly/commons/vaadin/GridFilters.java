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
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.HasInterval;
import net.tangly.bus.core.HasTags;
import org.jetbrains.annotations.NotNull;

public class GridFilters<E> extends HorizontalLayout {
    private final ListDataProvider<E> provider;
    private final List<GridFilter<E>> filters;


    public GridFilters(@NotNull ListDataProvider<E> provider) {
        this.provider = provider;
        filters = new ArrayList<>();
    }

    public void addFilter(@NotNull GridFilter<E> filter) {
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

        public GridFilterTags(@NotNull GridFilters container) {
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

        public GridFilterInterval(@NotNull GridFilters container) {
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

        public GridFilterText(@NotNull GridFilters container, @NotNull Function<E, String> getter, String label, String placeholder) {
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
