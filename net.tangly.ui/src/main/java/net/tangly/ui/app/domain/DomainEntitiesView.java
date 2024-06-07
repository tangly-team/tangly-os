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
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

/**
 * Displays all entities of a bounded domain with administration information. The view is useful during development of a new release or when evaluating the usage of a domain within
 * a specific application configuration.
 */
public class DomainEntitiesView extends ItemView<DomainEntity> {
    public DomainEntitiesView(@NotNull BoundedDomainUi<?> domain) {
        super(DomainEntity.class, domain, ProviderInMemory.of(domain.domain().entities()), null, Mode.LIST);
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(DomainEntity::domain).setKey(DOMAIN).setHeader(DOMAIN_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(DomainEntity::name).setKey(NAME).setHeader(NAME_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(o -> o.clazz().getName()).setKey("class").setHeader("Class").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(DomainEntity::hasOid).setKey("hasOid").setHeader("Has Oid").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::hasId).setKey("hasId").setHeader("Has Id").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::hasComments).setKey("hasComments").setHeader("Has Comments").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::hasTags).setKey("hasTags").setHeader("Has Tags").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::entitiesCount).setKey("entitiesCount").setHeader("#Entities").setSortable(true).setAutoWidth(true);
        grid.addColumn(o -> o.hasComments() ? o.commentsCount() : 0).setKey("commentsCount").setHeader("#Comments").setSortable(true).setAutoWidth(true);
        grid.addColumn(o -> o.hasTags() ? o.tagsCount() : 0).setKey("tagsCount").setHeader("#Tags").setSortable(true).setAutoWidth(true);
        HeaderRow headerRow = createHeaderRow();
    }
}
