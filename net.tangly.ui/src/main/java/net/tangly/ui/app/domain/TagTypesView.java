/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.grid.HeaderRow;
import net.tangly.core.TagType;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.components.GridMenu;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays all tags and their usage, often used for administrative information for a bounded domain.
 */
public class TagTypesView extends ItemView<TagType> {
    public static final String NAMESPACE = "namespace";
    public static final String NAMESPACE_LABEL = "Namespace";
    public static final String NAME = "name";
    public static final String NAME_LABEL = " Name";
    private final transient HashMap<TagType<?>, Integer> counts;

    public TagTypesView(@NotNull BoundedDomainUi<?> domain) {
        super(TagType.class, domain, ProviderInMemory.of(domain.domain().registry().tagTypes()), new TagTypeFilter(), Mode.LIST);
        this.counts = new HashMap<>();
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(TagType::namespace).setKey(NAMESPACE).setHeader(NAMESPACE_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::name).setKey(NAME).setHeader("Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::canHaveValue).setKey("canHaveValue").setHeader("Can Have Value").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(TagType::kind).setKey("Kind").setHeader("Kind").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> e.clazz().getSimpleName()).setKey("valueType").setHeader("Value Type").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(this::count).setKey("count").setHeader("Count").setSortable(true).setAutoWidth(true).setResizable(true);

        if (filter() instanceof TagTypeFilter filter) {
            HeaderRow headerRow = createHeaderRow();
            headerRow.getCell(grid.getColumnByKey(NAMESPACE)).setComponent(createTextFilterField(filter::namespace));
            headerRow.getCell(grid.getColumnByKey(NAME)).setComponent(createTextFilterField(filter::name));
        }
    }

    @Override
    protected void addActions(@NotNull GridMenu<TagType> menu) {
        super.addActions(menu);
        menu().add("Count Tags", e -> update(this.domain().countTags(new HashMap<>())), GridMenu.MenuItemType.GLOBAL);
    }

    static class TagTypeFilter extends ItemFilter<TagType> {
        private String namespace;
        private String name;

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
        this.counts.clear();
        this.counts.putAll(counts);
        grid().getDataProvider().refreshAll();
    }

    private int count(@NotNull TagType<?> type) {
        return counts.getOrDefault(type, 0);
    }
}
