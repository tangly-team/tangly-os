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
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import net.tangly.core.EmailAddress;
import net.tangly.core.GenderCode;
import net.tangly.core.PhoneNr;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.CrmTags;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.domain.NaturalEntity;
import net.tangly.erp.crm.domain.VcardType;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.util.Objects;

/**
 * Regular CRUD view on the natural entity abstraction. The grid and edition dialog are optimized for usability.
 */
@PageTitle("crm-natural-entities")
class NaturalEntitiesView extends EntityView<NaturalEntity> {
    static class NaturalEntityForm extends MutableEntityForm<NaturalEntity, NaturalEntitiesView> {
        private final Image image;

        public NaturalEntityForm(@NotNull NaturalEntitiesView view) {
            super(view, NaturalEntity::new);
            image = new Image();
            image.setWidth(200, Unit.PIXELS);
            image.setHeight(200, Unit.PIXELS);
            super.initEntityForm();
            addTabAt("details", details(), 1);
        }

        @Override
        public void value(NaturalEntity value) {
            super.value(value);
            if (Objects.nonNull(value) && (value.hasPhoto())) {
                image.setSrc(new StreamResource("photo.jpg", () -> new ByteArrayInputStream(value.photo().data())));
            } else {
                image.setSrc(new StreamResource("photo.jpg", () -> new ByteArrayInputStream(new byte[0])));
            }
        }

        private FormLayout details() {
            FormLayout form = new FormLayout();
            TextField firstname = VaadinUtils.createTextField("Firstname", "firstname");
            TextField lastname = VaadinUtils.createTextField("Lastname", "lastname");
            ComboBox<GenderCode> gender = ItemForm.createCodeField(CodeType.of(GenderCode.class), "Gender");
            TextField mobilePhone = VaadinUtils.createTextField("Mobile Phone", "mobile phone number", true);
            EmailField homeEmail = new EmailField("Home Email");
            homeEmail.setClearButtonVisible(true);
            TextField homeSite = VaadinUtils.createTextField("Home Site", "home site", true);

            form.add(firstname, lastname, gender);
            form.add(mobilePhone, homeEmail, homeSite);

            form.add(new HtmlComponent("br"));
            form.add(new VerticalLayout(image));

            binder().bind(firstname, NaturalEntity::firstname, NaturalEntity::firstname);
            binder().bind(lastname, NaturalEntity::name, NaturalEntity::name);
            binder().bind(gender, NaturalEntity::gender, NaturalEntity::gender);
            binder().bind(mobilePhone, e -> e.phoneNr(VcardType.mobile).map(PhoneNr::number).orElse(null), null);
            binder().bind(homeEmail, e -> e.email(VcardType.home).map(EmailAddress::text).orElse(null), null);
            binder().bind(homeSite, e -> e.site(VcardType.home).orElse(null), null);
            return form;
        }
    }


    public NaturalEntitiesView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        super(NaturalEntity.class, domain, domain.domain().realm().naturalEntities(), mode);
        form(() -> new NaturalEntityForm(this));
        init();
    }

    public static void defineOne2ManyEmployees(@NotNull Grid<Employee> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Employee::oid).setKey(OID).setHeader(OID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(o -> o.organization().name()).setKey("organization").setHeader("Organization").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.value(CrmTags.CRM_EMPLOYEE_TITLE).orElse(null)).setKey("title").setHeader("Title").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(Employee::from, ItemView.ISO_DATE_FORMAT)).setKey(ItemView.FROM).setHeader(ItemView.FROM_LABEL)
            .setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(Employee::to, ItemView.ISO_DATE_FORMAT)).setKey(ItemView.TO).setHeader(ItemView.TO_LABEL).setAutoWidth(true)
            .setResizable(true).setSortable(true);
    }

    private void init() {
        initEntityView();
        var grid = grid();
        VaadinUtils.addColumn(grid, NaturalEntity::name, "lastname", "Last Name");
        VaadinUtils.addColumn(grid, NaturalEntity::firstname, "firstname", "First Name");
        VaadinUtils.addColumn(grid, new ComponentRenderer<>(o -> (o.gender() == GenderCode.male) ? new Icon(VaadinIcon.MALE) : new Icon(VaadinIcon.FEMALE)),
            "gender", "Gender");
        VaadinUtils.addColumn(grid, VaadinUtils.linkedInComponentRenderer(CrmTags::linkedInTag, false), "linkedIn", "LinkedIn");
    }
}
