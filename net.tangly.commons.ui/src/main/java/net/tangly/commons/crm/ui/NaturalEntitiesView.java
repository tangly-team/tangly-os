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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.providers.ProviderView;
import net.tangly.commons.vaadin.CommentsView;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.commons.vaadin.One2ManyView;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.commons.vaadin.TagsView;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

public class NaturalEntitiesView extends CrmEntitiesView<NaturalEntity> {
    public NaturalEntitiesView(@NotNull Crm crm, @NotNull Mode mode) {
        super(crm, NaturalEntity.class, mode, NaturalEntitiesView::defineNaturalEntityGrid, crm.naturalEntities());
    }

    public static void defineNaturalEntityGrid(@NotNull Grid<NaturalEntity> grid) {
        InternalEntitiesView.defineGrid(grid);
        grid.addColumn(NaturalEntity::lastname).setKey("lastname").setHeader("Last Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(NaturalEntity::firstname).setKey("firstname").setHeader("First Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(CrmTags::individualLinkedInUrl).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true);
    }

    public static void defineOne2ManyEmployees(@NotNull Grid<Employee> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Employee::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(o -> o.organization().name()).setKey("organization").setHeader("Organization").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.tag(CrmTags.CRM_EMPLOYEE_TITLE).orElse(null)).setKey("title").setHeader("Title").setAutoWidth(true).setResizable(true)
                .setSortable(true);
        grid.addColumn(Employee::fromDate).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Employee::toDate).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull Mode mode, NaturalEntity entity) {
        NaturalEntity workedOn = (entity != null) ? entity : create();
        tabs.add(new Tab("Overview"), createOverallView(mode, workedOn));
        tabs.add(new Tab("Comments"), new CommentsView(mode, workedOn));
        tabs.add(new Tab("Tags"), new TagsView(mode, workedOn, crm().tagTypeRegistry()));
        One2ManyView<Employee> employees = new One2ManyView<>(Employee.class, mode, NaturalEntitiesView::defineOne2ManyEmployees,
                ProviderView.of(crm().employees(), o -> entity.oid() == o.person().oid()), new EmployeesView(crm(), mode));
        tabs.add(new Tab("Employees"), employees);
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull NaturalEntity entity) {
        boolean readonly = Mode.readOnly(mode);
        EntityField entityField = new EntityField();
        entityField.setReadOnly(readonly);
        TextField firstname = VaadinUtils.createTextField("Firstname", "firstname", readonly);
        TextField lastname = VaadinUtils.createTextField("Lastname", "lastname", readonly);
        TextField mobilePhone = VaadinUtils.createTextField("Mobile Phone", "mobile phone number", true);
        EmailField homeEmail = new EmailField("Home Email");
        homeEmail.setReadOnly(readonly);
        homeEmail.setClearButtonVisible(true);
        TextField homeSite = VaadinUtils.createTextField("Home Site", "home site", true);

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField);

        form.add(new HtmlComponent("br"));
        form.add(firstname, 1);
        form.add(lastname, 1);

        form.add(new HtmlComponent("br"));
        form.add(mobilePhone, homeEmail, homeSite);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.bind(firstname, NaturalEntity::firstname, NaturalEntity::firstname);
        binder.bind(lastname, NaturalEntity::lastname, NaturalEntity::lastname);
        binder.bind(mobilePhone, e -> e.phoneNr(CrmTags.MOBILE).map(PhoneNr::number).orElse(null), null);
        binder.bind(homeEmail, e -> e.email(CrmTags.HOME).orElse(null), null);
        binder.bind(homeSite, e -> e.site(CrmTags.HOME).orElse(null), null);
        binder.readBean(entity);
        return form;
    }

    @Override
    protected NaturalEntity create() {
        return new NaturalEntity();
    }
}
