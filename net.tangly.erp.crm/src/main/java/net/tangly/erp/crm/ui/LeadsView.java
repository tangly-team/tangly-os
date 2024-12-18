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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.EmailAddress;
import net.tangly.core.GenderCode;
import net.tangly.core.PhoneNr;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.ActivityCode;
import net.tangly.erp.crm.domain.Lead;
import net.tangly.erp.crm.domain.LeadCode;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Regular CRUD view on leads abstraction. The grid and edition dialog are optimized for usability.
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
        private DatePicker date;
        private TextField firstname;
        private TextField lastname;
        private ComboBox<GenderCode> gender;
        private TextField company;
        private TextField phoneNr;
        private TextField email;
        private TextField linkedIn;
        private ComboBox<LeadCode> leadCode;
        private ComboBox<ActivityCode> activityCode;
        private AsciiDocField text;

        public LeadForm(@NotNull LeadsView view) {
            super(view);
            addTabAt("details", details(), 0);
            addTabAt("text", textForm(text), 1);
        }

        private FormLayout details() {
            date = new DatePicker("Date");
            firstname = new TextField("Firstname", "firstname");
            lastname = new TextField("Lastname", "lastname");
            lastname.setRequired(true);
            gender = ItemForm.createCodeField(CodeType.of(GenderCode.class), "Gender");
            company = new TextField("Company", "company");
            phoneNr = new TextField("Phone", "phone number");
            email = new TextField("Email", "email");
            linkedIn = new TextField("Linked", "linkedIn");
            leadCode = ItemForm.createCodeField(CodeType.of(LeadCode.class), "Lead Code");
            text = new AsciiDocField("Text");

            var form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(date, firstname, lastname, gender, company, phoneNr, email, linkedIn, leadCode, activityCode);

            binder().bindReadOnly(date, Lead::date);
            binder().bindReadOnly(firstname, Lead::firstname);
            binder().bindReadOnly(lastname, Lead::lastname);
            binder().bindReadOnly(gender, Lead::gender);
            binder().bindReadOnly(company, Lead::company);
            binder().bindReadOnly(phoneNr, o -> Objects.nonNull(o.phoneNr()) ? o.phoneNr().number() : null);
            binder().bindReadOnly(email, o -> Objects.nonNull(o.email()) ? o.email().text() : null);
            binder().bindReadOnly(linkedIn, Lead::linkedIn);
            binder().bindReadOnly(leadCode, Lead::code);
            binder().bindReadOnly(text, Lead::text);
            return form;
        }

        @Override
        protected Lead createOrUpdateInstance(Lead entity) throws ValidationException {
            return new Lead(date.getValue(), leadCode.getValue(), firstname.getValue(), lastname.getValue(), gender.getValue(), company.getValue(),
                PhoneNr.of(phoneNr.getValue()), EmailAddress.of(email.getValue()), linkedIn.getValue(), text.getValue());
        }
    }

    public LeadsView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        super(Lead.class, domain, domain.domain().realm().leads(), new LeadFilter(), mode);
        form(() -> new LeadForm(this));
        init();
    }

    private void init() {
        var grid = grid();
        VaadinUtils.addColumn(grid, Lead::date, DATE, DATE_LABEL);
        VaadinUtils.addColumn(grid, Lead::code, "code", "Code");
        VaadinUtils.addColumn(grid, Lead::firstname, "firstname", "Firstname");
        VaadinUtils.addColumn(grid, Lead::lastname, "lastname", "Lastname");
        VaadinUtils.addColumn(grid,
            new ComponentRenderer<>(lead -> (lead.gender() == GenderCode.male) ? new Icon(VaadinIcon.MALE) : new Icon(VaadinIcon.FEMALE)), "gender", "Gender");
        VaadinUtils.addColumn(grid, Lead::company, "company", "Company");
        VaadinUtils.addColumn(grid, o -> (Objects.nonNull(o.phoneNr()) ? o.phoneNr().number() : null), "phoneNr", "Phone");
        VaadinUtils.addColumn(grid, VaadinUtils.emailAddressComponentRenderer(Lead::email), "email", "Email");
        VaadinUtils.addColumn(grid, VaadinUtils.linkedInComponentRenderer(Lead::linkedIn, false), "linkedIn", "LinkedIn");
    }
}
