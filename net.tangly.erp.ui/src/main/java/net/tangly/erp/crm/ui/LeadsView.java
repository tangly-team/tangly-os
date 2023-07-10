/*
 * Copyright 2006-2022 Marcel Baumann
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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.codes.CodeType;
import net.tangly.core.crm.GenderCode;
import net.tangly.erp.crm.domain.Lead;
import net.tangly.erp.crm.domain.LeadCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Regular CRUD view on leads abstraction. The grid and edition dialog wre optimized for usability.
 */

@PageTitle("crm-leads")
public class LeadsView extends ItemView<Lead> {
    static class LeadFilter extends ItemFilter<Lead> {
        @Override
        public boolean test(@NotNull Lead entity) {
            return true;
        }
    }

    static class LeadForm extends ItemForm<Lead, LeadsView> {
        public LeadForm(@NotNull LeadsView parent) {
            super(parent);
        }

        @Override
        protected void init() {
            DatePicker date = new DatePicker("Date");
            TextField firstname = new TextField("Firstname", "firstname");
            TextField lastname = new TextField("Lastname", "lastname");
            lastname.setRequired(true);
            ComboBox<GenderCode> gender = ItemForm.createCodeField(CodeType.of(GenderCode.class), "Gender");
            TextField company = new TextField("Company", "company");
            TextField phoneNr = new TextField("Phone", "phone number");
            TextField email = new TextField("Email", "email");
            TextField linkedIn = new TextField("Linked", "linkedIn");
            ComboBox<LeadCode> code = ItemForm.createCodeField(CodeType.of(LeadCode.class), "Code");
            TextArea text = new TextArea("Text", "text");

            FormLayout form = new FormLayout();
            form.add(date, firstname, lastname, gender, company, phoneNr, email, linkedIn, code, text);
            form.add(text, 3);

            binder().bind(date, Lead::date, null);
            binder().bind(firstname, Lead::firstname, null);
            binder().bind(lastname, Lead::lastname, null);
            binder().bind(gender, Lead::gender, null);
            binder().bind(company, Lead::company, null);
            binder().bind(phoneNr, o -> o.phoneNr().number(), null);
            binder().bind(email, o -> o.email().text(), null);
            binder().bind(linkedIn, Lead::linkedIn, null);
            binder().bind(code, Lead::code, null);
            binder().bind(text, Lead::text, null);

        }

        @Override
        public void clear() {

        }

        @Override
        protected Lead createOrUpdateInstance(Lead entity) throws ValidationException {
            return null;
        }
    }

    public LeadsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Lead.class, domain, domain.realm().leads(), new LeadFilter(), mode);
        init();
    }

    @Override
    protected void init() {
        var grid = grid();
        grid.addColumn(Lead::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::firstname).setKey("firstname").setHeader("Firstname").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::lastname).setKey("lastname").setHeader("Lastname").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new ComponentRenderer<>(lead -> (lead.gender() == GenderCode.male) ? new Icon(VaadinIcon.MALE) : new Icon(VaadinIcon.FEMALE))).setHeader("Gender")
            .setAutoWidth(true).setResizable(true);
        grid.addColumn(Lead::company).setKey("company").setHeader("Company").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> (Objects.nonNull(o.phoneNr()) ? o.phoneNr().number() : null)).setKey("phoneNr").setHeader("Phone").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(o -> (Objects.nonNull(o.email()) ? o.email().text() : null)).setKey("email").setHeader("Email").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(Lead::linkedIn).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Lead::activity).setKey("activity").setHeader("Activity").setAutoWidth(true).setResizable(true).setSortable(true);
        //                new GridDecorators.FilterCode<>(filters, (CodeType<LeadCode>) domain.registry().find(LeadCode.class).orElseThrow(), Lead::code, "Code"));
    }
}
