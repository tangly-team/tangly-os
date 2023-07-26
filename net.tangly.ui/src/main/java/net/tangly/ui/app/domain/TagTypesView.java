/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.grid.HeaderRow;
import net.tangly.core.TagType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Displays all tags and their usage, often use for administrative information for a bounded domain.
 */
public class TagTypesView extends ItemView<TagType> {
    final String NAMESPACE = "namespace";
    final String NAMESPACE_LABEL = "Namespace";
    final String NAME = "name";
    final String NAME_LABEL = " Name";
    private final transient HashMap<TagType<?>, Integer> counts;

    public TagTypesView(@NotNull BoundedDomain<?, ?, ?, ?> domain) {
        super(TagType.class, domain, ProviderInMemory.of(domain.registry().tagTypes()), new TagTypeFilter(), Mode.LIST);
        this.counts = new HashMap<>();
        init();
    }

    @Override
    protected void init() {
        var grid = grid();

        grid.addColumn(TagType::namespace).setKey(NAMESPACE).setHeader(NAMESPACE_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::name).setKey(NAME).setHeader("Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::canHaveValue).setKey("canHaveValue").setHeader("Can Have Value").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::kind).setKey("Kind").setHeader("Kind").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> e.clazz().getSimpleName()).setKey("valueType").setHeader("Value Type").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(this::count).setKey("count").setHeader("Count").setSortable(true).setAutoWidth(true).setResizable(true);

        if (filter() instanceof TagTypeFilter filter) {
            grid().getHeaderRows().clear();
            HeaderRow headerRow = grid().appendHeaderRow();
            addFilterText(headerRow, NAMESPACE, filter::namespace);
            addFilterText(headerRow, NAME, filter::name);
        }

        // TODO addMenuSection(List.of(Map.entry("Count Tags", e -> update(domain.countTags(new HashMap<>())))));
    }

    static class TagTypeFilter extends ItemFilter<TagType> {
        private String namespace;
        private String name;
        private Optional<Boolean> canHaveValue;
        private TagType.ValueKinds kind;

        public TagTypeFilter() {
        }

        public void namespace(String namespace) {
            this.namespace = namespace;
            refresh();
        }

        public void name(String name) {
            this.name = name;
            refresh();
        }

        @Override
        public boolean test(@NotNull TagType entity) {
            return matches(entity.namespace(), namespace) && matches(entity.name(), name);
        }
    }

    public void update(@NotNull Map<TagType<?>, Integer> counts) {
        counts.clear();
        this.counts.putAll(counts);
        grid().getDataProvider().refreshAll();
    }

    private int count(@NotNull TagType<?> type) {
        return counts.getOrDefault(type, 0);
    }
}
