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

package net.tangly.commons.crm.products.ui;

import javax.inject.Inject;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.RealmProducts;
import net.tangly.commons.vaadin.InternalEntitiesView;
import org.jetbrains.annotations.NotNull;

public class AssignementsView extends InternalEntitiesView<Assignment> {
    private final RealmProducts realmProducts;

    @Inject
    public AssignementsView(@NotNull RealmProducts realmProducts, @NotNull Mode mode) {
        super(Assignment.class, mode, realmProducts.assignements(), realmProducts.tagTypeRegistry());
        this.realmProducts = realmProducts;
        initializeGrid();
    }

    @Override
    protected void initializeGrid() {
        Grid<Assignment> grid = grid();
        InternalEntitiesView.addQualifiedEntityColumns(grid);
        grid.addColumn(Assignment::collaboratorId).setKey("collaboratorId").setHeader("CollaboratorId").setSortable(true).setAutoWidth(true).setResizable(true);
        // grid.addColumn(e -> e.product().name()).setKey("project").setHeader("Project").setSortable(true).setAutoWidth(true).setResizable(true);
        addAndExpand(filterCriteria(grid()), grid(), createCrudButtons());
    }

    @Override
    protected Assignment updateOrCreate(Assignment entity) {
        Assignment assignment = (entity != null) ? entity : new Assignment();
        try {
            binder.writeBean(assignment);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        return assignment;
    }
}
