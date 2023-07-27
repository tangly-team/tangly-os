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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.TypeRegistry;
import net.tangly.core.crm.CrmTags;
import net.tangly.core.crm.LegalEntity;
import net.tangly.core.crm.VcardType;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on legal entities abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("crm-legal entities")
class LegalEntitiesView extends EntityView<LegalEntity> {
    private final transient CrmBoundedDomain domain;

    static class LegalEntityForm extends EntityForm<LegalEntity, LegalEntitiesView> {
        public LegalEntityForm(@NotNull LegalEntitiesView parent, @NotNull TypeRegistry registry) {
            super(parent, LegalEntity::new);
            init();
            addTabAt("details", details(), 1);
        }

        private FormLayout details() {
            TextField site = VaadinUtils.createTextField("Web Site", "website", false);
            TextField vatNr = VaadinUtils.createTextField("VAT Nr", "vatNr", false);
            FormLayout form = new FormLayout();

            binder().bind(site, e -> e.site(VcardType.work).orElse(null), (e, v) -> e.site(VcardType.work, v));
            binder().bind(vatNr, LegalEntity::vatNr, LegalEntity::vatNr);
            return form;
        }

        //        private VerticalLayout employees() {
        //            One2ManyView<Employee> employees =
        //                new One2ManyView<>(Employee.class, mode, LegalEntityForm::defineOne2ManyEmployees, ProviderView.of(parent().domain().realm().employees(), o -> entity
        //                .oid() == o
        //                .organization().oid()),
        //                    new EmployeesView(domain, mode));
        //            return employees;
        //        }

        private static void defineOne2ManyEmployees(@NotNull Grid<Employee> grid) {
            VaadinUtils.initialize(grid);
            grid.addColumn(Employee::oid).setKey(OID).setHeader(OID).setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
            grid.addColumn(Employee::name).setKey(NAME).setHeader(NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(o -> o.value(CrmTags.CRM_EMPLOYEE_TITLE).orElse(null)).setKey("title").setHeader("Title").setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(new LocalDateRenderer<>(Employee::from, ISO_DATE_FORMAT)).setKey(FROM).setHeader(FROM_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(new LocalDateRenderer<>(Employee::to, ISO_DATE_FORMAT)).setKey(TO).setHeader(TO_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        }
    }

    public LegalEntitiesView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(LegalEntity.class, domain, domain.realm().legalEntities(), mode);
        this.domain = domain;
        init();
    }

    public CrmBoundedDomain domain() {
        return domain;
    }

    @Override
    protected void init() {
        var grid = grid();
        addEntityColumns(grid);
        grid.addColumn(VaadinUtils.linkedInComponentRenderer(CrmTags::organizationLinkedInUrl)).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true);
        grid.addColumn(VaadinUtils.urlComponentRenderer(CrmTags.CRM_SITE_WORK)).setKey("webSite").setHeader("Web Site").setAutoWidth(true);
        grid.addColumn(o -> o.value(CrmTags.CRM_RESPONSIBLE).orElse(null)).setKey("responsible").setHeader("Responsible").setAutoWidth(true).setSortable(true);

        addEntityFilterFields(grid(), filter());
        buildMenu();
    }
}
