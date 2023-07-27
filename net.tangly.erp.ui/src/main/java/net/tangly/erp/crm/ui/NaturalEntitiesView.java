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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.EmailAddress;
import net.tangly.core.PhoneNr;
import net.tangly.core.TypeRegistry;
import net.tangly.core.codes.CodeType;
import net.tangly.core.crm.CrmTags;
import net.tangly.core.crm.GenderCode;
import net.tangly.core.crm.NaturalEntity;
import net.tangly.core.crm.VcardType;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityField;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on natural entities abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("crm-natural entities")
class NaturalEntitiesView extends EntityView<NaturalEntity> {
    static class NaturalEntityForm extends EntityForm<NaturalEntity, NaturalEntitiesView> {
        public NaturalEntityForm(@NotNull NaturalEntitiesView parent, @NotNull TypeRegistry registry) {
            super(parent, NaturalEntity::new);
            init();
        }

        @Override
        protected void init() {
            FormLayout form = new FormLayout();
            form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));
            EntityField<NaturalEntity> entityField = new EntityField<>();
            entityField.bind(binder(), true);
            TextField firstname = new TextField("Firstname", "firstname");
            TextField lastname = new TextField("Lastname", "lastname");
            ComboBox<GenderCode> gender = ItemForm.createCodeField(CodeType.of(GenderCode.class), "Gender");
            TextField mobilePhone = VaadinUtils.createTextField("Mobile Phone", "mobile phone number", true);
            EmailField homeEmail = new EmailField("Home Email");
            homeEmail.setClearButtonVisible(true);
            TextField homeSite = VaadinUtils.createTextField("Home Site", "home site", true);


            FormLayout details = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            details.add(new HtmlComponent("br"));
            details.add(firstname, lastname, gender);

            //            if (entity.hasPhoto()) {
            //                Image image = new Image(new StreamResource("photo.jpg", () -> new ByteArrayInputStream(entity.photo().data())), "photo");
            //                image.setWidth("200px");
            //                image.setHeight("300px");
            //                form.add(image);
            //            }

            details.add(new HtmlComponent("br"));
            details.add(mobilePhone, homeEmail, homeSite);

            //            entityField.bind(binder());
            binder().bind(firstname, NaturalEntity::firstname, NaturalEntity::firstname);
            binder().bind(lastname, NaturalEntity::name, NaturalEntity::name);
            binder().bind(gender, NaturalEntity::gender, NaturalEntity::gender);
            binder().bind(mobilePhone, e -> e.phoneNr(VcardType.mobile).map(PhoneNr::number).orElse(null), null);
            binder().bind(homeEmail, e -> e.email(VcardType.home).map(EmailAddress::text).orElse(null), null);
            binder().bind(homeSite, e -> e.site(VcardType.home).orElse(null), null);

        }

        @Override
        protected NaturalEntity createOrUpdateInstance(NaturalEntity entity) {
            return null;
        }
    }

    private final transient CrmBoundedDomain domain;

    public NaturalEntitiesView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(NaturalEntity.class, domain, domain.realm().naturalEntities(), mode);
        this.domain = domain;
        init();
    }

    public static void defineOne2ManyEmployees(@NotNull Grid<Employee> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Employee::oid).setKey(OID).setHeader(OID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(o -> o.organization().name()).setKey("organization").setHeader("Organization").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.value(CrmTags.CRM_EMPLOYEE_TITLE).orElse(null)).setKey("title").setHeader("Title").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(Employee::from, ISO_DATE_FORMAT)).setKey(FROM).setHeader(FROM_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(Employee::to, ISO_DATE_FORMAT)).setKey(TO).setHeader(TO_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected void init() {
        var grid = grid();
        addEntityColumns(grid);

        grid.addColumn(NaturalEntity::name).setKey("lastname").setHeader("Last Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(NaturalEntity::firstname).setKey("firstname").setHeader("First Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(new ComponentRenderer<>(o -> (o.gender() == GenderCode.male) ? new Icon(VaadinIcon.MALE) : new Icon(VaadinIcon.FEMALE))).setKey("gender").setHeader("Gender")
            .setResizable(true).setResizable(true);
        grid.addColumn(VaadinUtils.linkedInComponentRenderer(CrmTags::individualLinkedInUrl)).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true);

        addEntityFilterFields(grid(), filter());
        buildMenu();
    }

    protected void registerTabs(@NotNull TabSheet tabSheet, @NotNull Mode mode, @NotNull NaturalEntity entity) {
        //        One2ManyView<Employee> employees =
        //            new One2ManyView<>(Employee.class, mode, NaturalEntitiesView::defineOne2ManyEmployees, ProviderView.of(domain.realm().employees(), o -> entity.oid() == o
        //            .person().oid()),
        //                new EmployeesView(domain, mode));
        //        tabSheet.add("Employees", employees);
    }
}
