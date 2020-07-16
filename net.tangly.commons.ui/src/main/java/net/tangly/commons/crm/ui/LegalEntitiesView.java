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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import net.tangly.bus.core.Entity;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.providers.ProviderView;
import net.tangly.commons.vaadin.CommentsView;
import net.tangly.commons.vaadin.CrudForm;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.One2ManyView;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.commons.vaadin.TagsView;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

public class LegalEntitiesView extends CrmEntitiesView<LegalEntity> {
    public LegalEntitiesView(@NotNull Crm crm) {
        super(crm, LegalEntity.class, LegalEntitiesView::defineLegalEntityGrid, crm.legalEntities());
        grid().addColumn(o -> VaadinUtils.format(crm().invoicedAmount(o))).setKey("invoicedAmount").setHeader("Invoiced").setAutoWidth(true).setResizable(true).setSortable(true);

    }

    public static void defineLegalEntityGrid(@NotNull Grid<LegalEntity> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Entity::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(zefixComponentRenderer()).setKey("id").setHeader("Id");
        grid.addColumn(Entity::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Entity::fromDate).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Entity::toDate).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(linkedInComponentRenderer(CrmTags::organizationLinkedInUrl)).setKey("linkedIn").setHeader("LinkedIn");
        grid.addColumn(urlComponentRenderer(CrmTags.CRM_SITE_WORK)).setKey("webSite").setHeader("Web Site").setAutoWidth(true);
    }

    public static ComponentRenderer<Anchor, LegalEntity> zefixComponentRenderer() {
        return new ComponentRenderer<Anchor, LegalEntity>(item -> {
            Anchor anchor = new Anchor();
            anchor.setText(item.id());
            if ((item.id() != null) && item.id().startsWith("CHE-")) {
                anchor.setHref(CrmTags.organizationZefixUrl(item.id()));
                anchor.setTarget("_blank");
            }
            return anchor;
        });
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
    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull CrudForm.Operation operation, LegalEntity entity) {
        LegalEntity workedOn = (entity != null) ? entity : create();
        tabs.add(new Tab("Overview"), createOverallView(operation, workedOn));
        tabs.add(new Tab("Comments"), new CommentsView(workedOn));
        tabs.add(new Tab("Tags"), new TagsView(workedOn, crm().tagTypeRegistry()));
        One2ManyView<Employee> employees = new One2ManyView<>(Employee.class, Mode.EDITABLE, LegalEntitiesView::defineOne2ManyEmployees,
                ProviderView.of(crm().employees(), o -> entity.oid() == o.organization().oid()), new EmployeesView(crm()));
        tabs.add(new Tab("Employees"), employees);
    }

    @Override
    protected FormLayout createOverallView(@NotNull Operation operation, @NotNull LegalEntity entity) {
        boolean readonly = Operation.isReadOnly(operation);
        EntityField entityField = new EntityField();
        TextField site = VaadinUtils.createTextField("Web Site", "website", readonly);
        TextField vatNr = VaadinUtils.createTextField("VAT Nr", "vatNr", readonly);

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField, new HtmlComponent("br"), site, vatNr);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.bind(site, e -> e.site(CrmTags.WORK).orElse(null), (e, v) -> e.site(CrmTags.WORK, v));
        binder.bind(vatNr, LegalEntity::vatNr, LegalEntity::vatNr);
        binder.readBean(entity);
        return form;
    }

    @Override
    protected LegalEntity create() {
        return new LegalEntity();
    }
}
