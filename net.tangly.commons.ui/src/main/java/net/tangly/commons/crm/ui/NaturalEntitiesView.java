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

package net.tangly.commons.crm.ui;

import com.vaadin.flow.component.grid.Grid;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

public class NaturalEntitiesView extends CrmEntitiesView<NaturalEntity> {
    public NaturalEntitiesView(@NotNull Crm crm) {
        super(crm, NaturalEntity.class, NaturalEntitiesView::defineNaturalEntityGrid, crm.naturalEntities());
    }

    public static void defineNaturalEntityGrid(@NotNull Grid<NaturalEntity> grid) {
        EntitiesView.defineGrid(grid);
        grid.addColumn(NaturalEntity::lastname).setKey("lastname").setHeader("Last Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(NaturalEntity::firstname).setKey("firstname").setHeader("First Name").setSortable(true).setAutoWidth(true).setResizable(true);
    }

    @Override
    protected NaturalEntity create() {
        return new NaturalEntity();
    }
}
