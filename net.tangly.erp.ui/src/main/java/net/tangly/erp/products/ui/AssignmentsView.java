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

package net.tangly.erp.products.ui;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on assignments abstraction. The grid and the edition dialog are optimized for usability.
 */
@PageTitle("products-assignements")
class AssignmentsView extends EntityView<Assignment> {
    public AssignmentsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        super(Assignment.class, domain, domain.realm().assignments(), mode);
        initEntityView();
    }

    @Override
    protected void addActions(@NotNull GridContextMenu<Assignment> menu) {
        super.addActions(menu);
        menu().add(new Hr());
        menu().addItem("Print", e -> Cmd.ofItemCmd(e, (Assignment a) -> new CmdCreateAssignmentDocument(a, domain()).execute()));
        menu().add(new Hr());
        menu().addItem("Import", e -> Cmd.ofGlobalCmd(e, () -> new CmdFilesUploadEfforts(domain()).execute()));
    }

    @Override
    public ProductsBoundedDomain domain() {
        return (ProductsBoundedDomain) super.domain();
    }
}
