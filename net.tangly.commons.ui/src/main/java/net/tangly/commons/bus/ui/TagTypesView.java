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

package net.tangly.commons.bus.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.TagType;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class TagTypesView extends VerticalLayout {
    private final Grid<TagType> grid;
    private final TagTypeRegistry registry;
    private final HashMap<TagType, Integer> counts;

    public TagTypesView(TagTypeRegistry registry) {
        this.registry = registry;
        this.counts = new HashMap<>();
        this.grid = new Grid<>(TagType.class, false);

        grid.setDataProvider((ListDataProvider) DataProvider.ofCollection(registry.tagTypes()));

        grid.addColumn(TagType::namespace).setKey("namespace").setHeader("Namespace").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::name).setKey("name").setHeader("Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::canHaveValue).setKey("canHaveValue").setHeader("Can Have Value").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::kind).setKey("Kind").setHeader("Kind").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> e.clazz().getSimpleName()).setKey("valueType").setHeader("Value Type").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> this.count(e)).setKey("count").setHeader("Count").setSortable(true).setAutoWidth(true).setResizable(true);

        grid.setHeightFull();
        grid.setMinHeight("20em");
        grid.setWidthFull();
        setSizeFull();
        add(grid);
    }

    public void refreshData() {
        grid.getDataProvider().refreshAll();
    }

    public void addCounts(List<? extends HasTags> entities) {
        entities.stream().flatMap(e -> e.tags().stream()).map(registry::find).flatMap(Optional::stream).forEach(this::increment);
    }

    public void clearCounts() {
        counts.clear();
    }

    int count(@NotNull TagType<?> type) {
        return counts.containsKey(type) ? counts.get(type) : -1;
    }

    private void increment(@NotNull TagType<?> type) {
        if (!counts.containsKey(type)) {
            counts.put(type, 0);
        }
        counts.put(type, counts.get(type) + 1);
    }
}
