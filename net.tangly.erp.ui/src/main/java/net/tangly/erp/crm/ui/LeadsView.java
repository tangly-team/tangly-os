/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import java.util.Objects;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import net.tangly.core.EmailAddress;
import net.tangly.core.PhoneNr;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.ActivityCode;
import net.tangly.erp.crm.domain.GenderCode;
import net.tangly.erp.crm.domain.Lead;
import net.tangly.erp.crm.domain.LeadCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.CodeField;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.components.VaadinUtils;
import net.tangly.ui.grids.GridDecorators;
import net.tangly.ui.grids.PaginatedGrid;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on leads abstraction. The grid and edition dialog wre optimized for usability.
 */
class LeadsView extends EntitiesView<Lead> {
    private final transient CrmBoundedDomain domain;
    private DatePicker date;
    private CodeField<LeadCode> code;
    private TextField firstname;
    private TextField lastname;
    private CodeField<GenderCode> gender;
    private TextField company;
    private TextField phoneNr;
    private TextField email;
    private TextField linkedIn;
    private CodeField<ActivityCode> activity;
    private TextArea text;
    private Binder<Lead> binder;

    public LeadsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Lead.class, mode, domain.realm().leads());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        PaginatedGrid<Lead> grid = grid();
        grid.addColumn(Lead::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::firstname).setKey("firstname").setHeader("Firstname").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::lastname).setKey("lastname").setHeader("Lastname").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new ComponentRenderer<>(lead -> (lead.gender() == GenderCode.male) ? new Icon(VaadinIcon.MALE) : new Icon(VaadinIcon.FEMALE)))
            .setHeader("Gender").setAutoWidth(true).setResizable(true);
        grid.addColumn(Lead::company).setKey("company").setHeader("Company").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> (Objects.nonNull(o.phoneNr()) ? o.phoneNr().number() : null)).setKey("phoneNr").setHeader("Phone").setAutoWidth(true)
            .setResizable(true).setSortable(true);
        grid.addColumn(o -> (Objects.nonNull(o.email()) ? o.email().text() : null)).setKey("email").setHeader("Email").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(Lead::linkedIn).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::activity).setKey("activity").setHeader("Activity").setAutoWidth(true).setResizable(true).setSortable(true);
        addAndExpand(filterCriteria(false, false, filters -> {
            filters.addFilter(new GridDecorators.FilterDate<>(filters));
            filters.addFilter(
                new GridDecorators.FilterCode<>(filters, (CodeType<LeadCode>) domain.registry().find(LeadCode.class).orElseThrow(), Lead::code, "Code"));
        }), grid(), gridButtons());
    }

    @Override
    protected Lead updateOrCreate(Lead entity) {
        return new Lead(date.getValue(), code.getValue(), firstname.getValue(), lastname.getValue(), gender.getValue(), company.getValue(),
            PhoneNr.of(phoneNr.getValue()), EmailAddress.of(email.getValue()), linkedIn.getValue(), activity.getValue(), text.getValue());
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Lead entity, FormLayout form) {
        date = new DatePicker("Date");
        firstname = new TextField("Firstname", "firstname");
        lastname = new TextField("Lastname", "lastname");
        lastname.setRequired(true);
        gender = new CodeField<>(CodeType.of(GenderCode.class), "Gender");
        company = new TextField("Company", "company");
        phoneNr = new TextField("Phone", "phone number");
        email = new TextField("Email", "email");
        linkedIn = new TextField("Linked", "linkedIn");
        code = new CodeField<>(CodeType.of(LeadCode.class), "Code");
        text = new TextArea("Text", "text");

        VaadinUtils.readOnly(operation, date, firstname, lastname, gender, company, phoneNr, email, linkedIn, code, text);

        form.add(date, firstname, lastname, gender, company, phoneNr, email, linkedIn, code, text);
        form.add(text, 3);

        binder = new Binder<>();
        binder.bind(date, Lead::date, null);
        binder.bind(firstname, Lead::firstname, null);
        binder.bind(lastname, Lead::lastname, null);
        binder.bind(gender, Lead::gender, null);
        binder.bind(company, Lead::company, null);
        binder.bind(phoneNr, o -> o.phoneNr().number(), null);
        binder.bind(email, o -> o.email().text(), null);
        binder.bind(linkedIn, Lead::linkedIn, null);
        binder.bind(code, Lead::code, null);
        binder.bind(text, Lead::text, null);
        if (entity != null) {
            binder.readBean(entity);
        }
        return form;
    }
}
