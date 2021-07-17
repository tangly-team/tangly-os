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

package net.tangly.erp.products.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.grid.Grid;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.components.InternalEntitiesView;
import net.tangly.ui.grids.GridDecorators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on assignments abstraction. The grid and edition dialog wre optimized for usability.
 */
class AssignmentsView extends InternalEntitiesView<Assignment> {
    private static final Logger logger = LogManager.getLogger();
    private final transient ProductsBoundedDomain domain;

    @Inject
    public AssignmentsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        super(Assignment.class, mode, domain.realm().assignments(), domain.registry());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        Grid<Assignment> grid = grid();
        InternalEntitiesView.addQualifiedEntityColumns(grid);
        grid.addColumn(Assignment::collaboratorId).setKey("collaboratorId").setHeader("Collaborator").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> (e.product() != null) ? e.product().name() : null).setKey("project").setHeader("Project").setSortable(true).setAutoWidth(true)
            .setResizable(true);
        GridDecorators<Assignment> decorators = filterCriteria(true, false, InternalEntitiesView::addQualifiedEntityFilters);
        decorators.addItemAction("Add Effort", e -> new CmdCreateEffort(selectedItem(), domain).execute());
        decorators.addItemAction("Print", e -> new CmdCreateAssignmentDocument(selectedItem(), domain).execute());
        addAndExpand(decorators, grid(), gridButtons());
    }

    @Override
    protected Assignment updateOrCreate(Assignment entity) {
        return EntitiesView.updateOrCreate(entity, binder, Assignment::new);
    }
}
