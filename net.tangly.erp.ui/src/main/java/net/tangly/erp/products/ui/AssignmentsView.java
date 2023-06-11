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

package net.tangly.erp.products.ui;

import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.EntityView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on assignments abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("products-assignements")
class AssignmentsView extends EntityView<Assignment> {
    private static final Logger logger = LogManager.getLogger();

    public AssignmentsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        super(Assignment.class, domain, domain.realm().assignments(), mode);
        init();
    }

    @Override
    protected void init() {
        addEntityColumns(grid());
        addEntityFilters(grid(), filter());
        //        decorators.addItemAction("Add Effort", e -> new CmdCreateEffort(value(), domain).execute());
        //        decorators.addItemAction("Print", e -> new CmdCreateAssignmentDocument(value(), domain).execute());
        buildMenu();
    }
}
