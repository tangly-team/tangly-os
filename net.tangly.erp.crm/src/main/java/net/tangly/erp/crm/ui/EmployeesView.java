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

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.crm.domain.CrmTags;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.domain.NaturalEntity;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.MutableEntityForm;
import net.tangly.ui.components.One2OneField;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on employees abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("crm-employees")
class EmployeesView extends EntityView<Employee> {
    static class EmployeeForm extends MutableEntityForm<Employee, EmployeesView> {
        public EmployeeForm(@NotNull EmployeesView parent) {
            super(parent, Employee::new);
            initEntityForm();
            addTabAt("details", details(), 1);
        }

        private FormLayout details() {
            One2OneField<LegalEntity> organization = new One2OneField<>("Organization", LegalEntity.class, parent().domain().realm().legalEntities());
            One2OneField<NaturalEntity> person = new One2OneField<>("Person", NaturalEntity.class, parent().domain().realm().naturalEntities());
            FormLayout form = new FormLayout();
            form.add(person);
            form.add(new HtmlComponent("br"));
            form.add(organization);

            binder().forField(organization).bind(Employee::organization, Employee::organization);
            binder().forField(person).bind(Employee::person, Employee::person);
            return form;
        }
    }

    public EmployeesView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        super(Employee.class, domain, domain.domain().realm().employees(), mode);
        form(() -> new EmployeeForm(this));
        init();
    }

    @Override
    public CrmBoundedDomain domain() {
        return (CrmBoundedDomain) super.domain();
    }

    private void init() {
        var grid = grid();
        addEntityColumns(grid);
        grid.addColumn(o -> o.value(CrmTags.CRM_EMPLOYEE_TITLE).orElse(null)).setKey("title").setHeader("Title").setAutoWidth(true).setResizable(true).setSortable(true);
        addEntityFilterFields(grid(), filter());
    }
}
