/*
 * Copyright 2006-2021 Marcel Baumann
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

package net.tangly.commons.domain.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.components.grids.GridDecorators;
import net.tangly.components.grids.PaginatedGrid;
import net.tangly.core.TagType;
import net.tangly.core.domain.BoundedDomain;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays all tags and their usage, often use for administrative information for a bounded domain.
 */
public class TagTypesView extends VerticalLayout {
    private final PaginatedGrid<TagType<?>> grid;
    private final transient BoundedDomain<?, ?, ?, ?> domain;
    private final transient HashMap<TagType<?>, Integer> counts;

    public TagTypesView(@NotNull BoundedDomain<?, ?, ?, ?> domain) {
        this.domain = domain;
        this.counts = new HashMap<>();
        this.grid = new PaginatedGrid<>();
        initialize();
    }

    protected void initialize() {
        grid.setPageSize(10);
        grid.paginatorSize(3);
        grid.dataProvider(DataProvider.ofCollection(domain.registry().tagTypes()));

        grid.addColumn(TagType::namespace).setKey("namespace").setHeader("Namespace").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::name).setKey("name").setHeader("Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::canHaveValue).setKey("canHaveValue").setHeader("Can Have Value").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::kind).setKey("Kind").setHeader("Kind").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> e.clazz().getSimpleName()).setKey("valueType").setHeader("Value Type").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(this::count).setKey("count").setHeader("Count").setSortable(true).setAutoWidth(true).setResizable(true);

        grid.setHeightFull();
        grid.setMinHeight("5em");
        grid.setWidthFull();

        GridDecorators<TagType<?>> decorator = new GridDecorators<>(grid, TagType.class.getSimpleName(), false, true);
        decorator.addGlobalAction("Count Tags", e -> update(domain.countTags(new HashMap<>())));

        setSizeFull();
        addAndExpand(decorator, grid);
    }

    public void update(@NotNull Map<TagType<?>, Integer> counts) {
        counts.clear();
        this.counts.putAll(counts);
        grid.getDataProvider().refreshAll();
    }

    private int count(@NotNull TagType<?> type) {
        return counts.getOrDefault(type, 0);
    }
}
