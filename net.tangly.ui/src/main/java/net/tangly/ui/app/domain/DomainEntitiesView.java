/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.grids.GridDecorators;
import net.tangly.ui.grids.PaginatedGrid;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

/**
 * Displays all entities of a bounded domain with administration information. The view is useful during development of a new release or when evaluating the
 * usage of a domain within a specific application configuration.
 */
public class DomainEntitiesView extends EntitiesView<DomainEntity> {
    private final PaginatedGrid<DomainEntity<?>> grid;
    private final BoundedDomain<?, ?, ?, ?> domain;

    public DomainEntitiesView(@NotNull BoundedDomain<?, ?, ?, ?> domain) {
        super(DomainEntity.class, Mode.IMMUTABLE, new ProviderInMemory(domain.entities()));
        this.domain = domain;
        grid = new PaginatedGrid<>();
        initialize();
    }

    @Override
    protected void initialize() {
        grid.setPageSize(10);
        grid.paginatorSize(3);

        grid.dataProvider(new ListDataProvider<>(domain.entities()));
        grid.addColumn(DomainEntity::domain).setKey("domain").setHeader("Domain").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(DomainEntity::name).setKey("name").setHeader("Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(o -> o.clazz().getName()).setKey("class").setHeader("Class").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(DomainEntity::hasOid).setKey("hasOid").setHeader("Has Oid").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::hasId).setKey("hasId").setHeader("Has Id").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::hasComments).setKey("hasComments").setHeader("Has Comments").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::hasTags).setKey("hasTags").setHeader("Has Tags").setSortable(true).setAutoWidth(true);
        grid.addColumn(DomainEntity::EntitiesCount).setKey("entitiesCount").setHeader("#Entities").setSortable(true).setAutoWidth(true);
        grid.addColumn(o -> o.hasComments() ? o.CommentsCount() : 0).setKey("commentsCount").setHeader("#Comments").setSortable(true).setAutoWidth(true);
        grid.addColumn(o -> o.hasTags() ? o.TagsCount() : 0).setKey("tagsCount").setHeader("#Tags").setSortable(true).setAutoWidth(true);
        grid.setHeightFull();
        grid.setMinHeight("5em");
        grid.setWidthFull();
        setSizeFull();

        // TODO why create decorator
        GridDecorators<DomainEntity<?>> decorator = new GridDecorators(grid, entityClass().getSimpleName(),false, false);
        decorator.addFilter(new GridDecorators.FilterText<>(decorator, DomainEntity::name, "Name", "name"));
        addAndExpand(decorator, grid);
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, DomainEntity entity, @NotNull FormLayout form) {
        return form;
    }

    @Override
    protected DomainEntity updateOrCreate(DomainEntity entity) {
        return null;
    }
}
