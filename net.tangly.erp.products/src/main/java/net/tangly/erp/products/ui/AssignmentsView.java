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

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Product;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.MutableEntityForm;
import net.tangly.ui.components.One2OneField;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on the assignment abstraction. The grid and the edition dialog are optimized for usability.
 */
@PageTitle("products-assignments")
class AssignmentsView extends EntityView<Assignment> {
    class AssignmentForm extends MutableEntityForm<Assignment, AssignmentsView> {
        public AssignmentForm(@NotNull AssignmentsView parent, @NotNull TypeRegistry registry) {
            super(parent, Assignment::new);
            initEntityForm();
            addTabAt("details", details(), 1);

        }

        private FormLayout details() {
            var form = new FormLayout();
            var productField = new One2OneField<>("Product", Product.class, domain().realm().products());
            var collaboratorId = new TextField("Collaborator ID");
            var closedPeriod = new DatePicker("Closed period");
            form.add(productField, collaboratorId);
            binder().bind(productField, Assignment::product, Assignment::product);
            binder().bind(collaboratorId, Assignment::collaboratorId, Assignment::collaboratorId);
            binder().bind(closedPeriod, Assignment::closedPeriod, Assignment::closedPeriod);
            return form;
        }
    }

    public AssignmentsView(@NotNull ProductsBoundedDomainUi domain,@NotNull Mode mode) {
        super(Assignment.class, domain, domain.assignments(), mode);
        form(() -> new AssignmentForm(this, domain.domain().registry()));
        init();
    }

    @Override
    protected void addActions(@NotNull GridContextMenu<Assignment> menu) {
        menu().add(new Hr());
        menu().addItem("Print", e -> Cmd.ofItemCmd(e, (Assignment o) -> new CmdCreateEffortsReport(o, domain()).execute()));
        menu().addItem("Import", e -> Cmd.ofGlobalCmd(e, () -> new CmdFilesUploadEfforts(domain()).execute()));
    }

    @Override
    public ProductsBoundedDomain domain() {
        return (ProductsBoundedDomain) super.domain();
    }

    private void init() {
        initEntityView();
        var grid = grid();
        grid.addColumn(Assignment::collaboratorId).setKey("collaboratorId").setHeader("Collaborator Id").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(Assignment::closedPeriod).setKey("closedPeriod").setHeader("Closed Period").setSortable(true).setAutoWidth(true).setResizable(true);
    }
}
