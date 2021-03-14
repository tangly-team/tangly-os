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

package net.tangly.commons.products.ui;

import java.lang.invoke.MethodHandles;
import javax.inject.Inject;

import com.vaadin.flow.component.grid.Grid;
import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.ProductsBoundedDomain;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.components.grids.GridDecorators;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regular CRUD view on assignments abstraction. The grid and edition dialog wre optimized for usability.
 */
class AssignmentsView extends InternalEntitiesView<Assignment> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
        grid.addColumn(e -> (e.product() != null) ? e.product().name() : null)
            .setKey("project")
            .setHeader("Project")
            .setSortable(true)
            .setAutoWidth(true)
            .setResizable(true);
        GridDecorators<Assignment> gridFunctions = gridFiltersAndActions(true, false);
        gridFunctions.addItemAction("Add Effort", e -> new CmdCreateEffort(selectedItem(), domain).execute());
        gridFunctions.addItemAction("Print", e -> new CmdCreateAssignmentDocument(selectedItem(), domain).execute());
        addAndExpand(filterCriteria(false, false, InternalEntitiesView::addQualifiedEntityFilters), grid(), gridButtons());
    }

    @Override
    protected Assignment updateOrCreate(Assignment entity) {
        return EntitiesView.updateOrCreate(entity, binder, Assignment::new);
    }
}
