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

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on employees abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("crm-employees")
class EmployeesView extends EntityView<Employee> {
    private final transient CrmBoundedDomain domain;

    @Override
    protected void init() {

    }

    static class EmployeeForm extends EntityForm<Employee, EmployeesView> {
        public EmployeeForm(@NotNull EmployeesView parent, @NotNull TypeRegistry registry) {
            super(parent, Employee.class);
            init();
        }

        public void init() {
            //            One2OneField<LegalEntity> organization = new One2OneField<>("Organization", new LegalEntitiesView(domain, mode));
            //            organization.setReadOnly(readonly);
            //            One2OneField<NaturalEntity> person = new One2OneField<>("Person", new NaturalEntitiesView(domain, mode));
            //            person.setReadOnly(readonly);

            FormLayout form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(new HtmlComponent("br"));
            //            form.add(person, organization);

            //            binder().forField(organization).bind(Employee::organization, Employee::organization);
            //            binder().forField(person).bind(Employee::person, Employee::person);
        }

        @Override
        protected Employee createOrUpdateInstance(Employee entity) throws ValidationException {
            return null;
        }
    }

    public EmployeesView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Employee.class, domain, domain.realm().employees(), mode);
        this.domain = domain;
        init();
    }
}
