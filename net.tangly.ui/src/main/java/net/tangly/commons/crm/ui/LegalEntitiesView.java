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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.providers.ViewProvider;
import net.tangly.commons.vaadin.CommentsView;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.commons.vaadin.One2ManyView;
import net.tangly.commons.vaadin.QualifiedEntityField;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.commons.vaadin.TagsView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class LegalEntitiesView extends InternalEntitiesView<LegalEntity> {
    private final CrmBoundedDomain domain;

    public LegalEntitiesView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(LegalEntity.class, mode, domain.realm().legalEntities(), domain.registry());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        Grid<LegalEntity> grid = grid();
        grid.addColumn(LegalEntity::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(VaadinUtils.zefixComponentRenderer()).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(LegalEntity::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(LegalEntity::fromDate).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(LegalEntity::toDate).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(VaadinUtils.linkedInComponentRenderer(CrmTags::organizationLinkedInUrl)).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true);
        grid.addColumn(VaadinUtils.urlComponentRenderer(CrmTags.CRM_SITE_WORK)).setKey("webSite").setHeader("Web Site").setAutoWidth(true);
        grid.addColumn(o -> o.tag(CrmTags.CRM_RESPONSIBLE).orElse(null)).setKey("responsible").setHeader("Responsible").setAutoWidth(true)
            .setSortable(true);

        addAndExpand(filterCriteria(grid()), grid(), gridButtons());
    }

    public static void defineOne2ManyEmployees(@NotNull Grid<Employee> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Employee::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(o -> o.person().name()).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.tag(CrmTags.CRM_EMPLOYEE_TITLE).orElse(null)).setKey("title").setHeader("Title").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(Employee::fromDate).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Employee::toDate).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull Mode mode, LegalEntity entity) {
        tabs.add(new Tab("Overview"), createOverallView(mode, entity));
        tabs.add(new Tab("Comments"), new CommentsView(mode, entity));
        tabs.add(new Tab("Tags"), new TagsView(mode, entity, domain.realm().tagTypeRegistry()));
        One2ManyView<Employee> employees = new One2ManyView<>(Employee.class, mode, LegalEntitiesView::defineOne2ManyEmployees,
            ViewProvider.of(domain.realm().employees(), o -> entity.oid() == o.organization().oid()), new EmployeesView(domain, mode));
        tabs.add(new Tab("Employees"), employees);
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull LegalEntity entity) {
        boolean readonly = Mode.readOnly(mode);
        QualifiedEntityField<LegalEntity> entityField = new QualifiedEntityField<>();
        entityField.setReadOnly(readonly);
        TextField site = VaadinUtils.createTextField("Web Site", "website", readonly);
        TextField vatNr = VaadinUtils.createTextField("VAT Nr", "vatNr", readonly);

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        entityField.addEntityComponentsTo(form);
        form.add(new HtmlComponent("br"), site, vatNr);
        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.bind(site, e -> e.site(CrmTags.Type.work).orElse(null), (e, v) -> e.site(CrmTags.Type.work, v));
        binder.bind(vatNr, LegalEntity::vatNr, LegalEntity::vatNr);
        binder.readBean(entity);
        return form;
    }

    @Override
    protected LegalEntity updateOrCreate(LegalEntity entity) {
        return EntitiesView.updateOrCreate(entity, binder, LegalEntity::new);
    }
}
