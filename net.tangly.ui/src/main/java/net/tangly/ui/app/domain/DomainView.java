/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import net.tangly.core.domain.BoundedDomain;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a view on the domain with tabs monitoring the instances of domain specific entities and tags used in the bounded domain. The list of domain entities is provided with
 * statistics. Similar information is available for tag usage in the domain.
 */
public class DomainView extends VerticalLayout {
    private static final String ENTITIES = "Entities";
    private static final String TAGS = "Tags";
    private final TabSheet tabSheet;
    private final transient BoundedDomain<?, ?, ?, ?> domain;

    public DomainView(@NotNull BoundedDomain<?, ?, ?, ?> domain) {
        this.domain = domain;
        tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_SMALL);
        initialize();
    }

    protected void initialize() {
        tabSheet.add(ENTITIES, new DomainEntitiesView(domain));
        tabSheet.add(TAGS, new TagTypesView(domain));
        add(tabSheet);
    }
}
