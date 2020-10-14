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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.RealmCrm;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.One2OneField;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class EmployeesView extends CrmEntitiesView<Employee> {
    public EmployeesView(@NotNull RealmCrm realmCrm, @NotNull Mode mode) {
        super(realmCrm, Employee.class, mode, realmCrm.employees());
        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        Grid<Employee> grid = grid();
        grid.addColumn(e -> e.person().name()).setKey("person").setHeader("Person").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> e.organization().name()).setKey("organization").setHeader("Organization").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(e -> e.tag(CrmTags.CRM_EMPLOYEE_TITLE).orElse("")).setKey("title").setHeader("Title").setSortable(true).setAutoWidth(true)
                .setResizable(true);
    }

    @Override
    protected Employee create() {
        return new Employee();
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull Employee entity) {
        boolean readonly = Mode.readOnly(mode);
        EntityField entityField = new EntityField();
        entityField.setReadOnly(readonly);
        One2OneField<LegalEntity, LegalEntitiesView> organization = new One2OneField<>("Organization", new LegalEntitiesView(realm(), mode));
        organization.setReadOnly(readonly);
        One2OneField<NaturalEntity, NaturalEntitiesView> person = new One2OneField<>("Person", new NaturalEntitiesView(realm(), mode));
        person.setReadOnly(readonly);

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField);
        form.add(new HtmlComponent("br"));
        form.add(organization);
        form.add(new HtmlComponent("br"));
        form.add(person);

        entityField.bind(binder);
        binder.forField(organization).bind(Employee::organization, Employee::organization);
        binder.forField(person).bind(Employee::person, Employee::person);
        binder.readBean(entity);
        return form;
    }
}
