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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import net.tangly.core.domain.AccessRights;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a view on the domain with tabs monitoring the instances of domain-specific entities and tags used in the bounded domain. The list of domain entities is provided with
 * statistics. Similar information is available for tag usage in the domain.
 */
public class DomainView extends VerticalLayout implements View {
    private static final String ENTITIES = "Entities";
    private static final String TAGS = "Tags";
    private final TabSheet tabSheet;
    private final transient BoundedDomainUi<?> domain;
    private AccessRights rights;

    public DomainView(@NotNull BoundedDomainUi<?> domain) {
        this.domain = domain;
        tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_SMALL);
        initialize();
    }

    private void initialize() {
        tabSheet.add(ENTITIES, new DomainEntitiesView(domain, domain.rights()));
        tabSheet.add(TAGS, new TagTypesView(domain, domain.rights()));
        add(tabSheet);
    }

    @Override
    public void rights(AccessRights rights) {
        this.rights = rights;
    }

    @Override
    public AccessRights rights() {
        return rights;
    }

    @Override
    public void refresh() {
        tabSheet.getChildren().forEach(component -> ((View) component).refresh());
    }
}
