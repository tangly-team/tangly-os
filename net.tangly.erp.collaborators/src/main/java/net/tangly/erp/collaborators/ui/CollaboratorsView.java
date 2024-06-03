/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.erp.collaborators.ui;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.erp.collaborators.domain.Collaborator;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

public class CollaboratorsView extends ItemView<Collaborator> {
    static class CollaboratorForm extends ItemForm<Collaborator, CollaboratorsView> {
        TextField id;
        TextField oldSsn;
        DatePicker birthday;
        TextField fullname;
        TextField internalId;
        AddressField address;

        CollaboratorForm(@NotNull CollaboratorsView parent) {
            super(parent);
            id = VaadinUtils.createTextField("Assignment", "assignment");
            oldSsn = VaadinUtils.createTextField("Old SSN", "old SSN");
            birthday = VaadinUtils.createDatePicker("Birthday");
            fullname = new TextField("Fullname", "fullname");
            internalId = new TextField("Internal Id", "internalId");
            address = new AddressField();
            addTabAt("details", details(), 0);
        }

        protected FormLayout details() {
            FormLayout form = new FormLayout();
            form.add(id, oldSsn, birthday, fullname, internalId, address);

            binder().bindReadOnly(id, Collaborator::id);
            binder().bindReadOnly(oldSsn, Collaborator::oldSocialSecurityNumber);
            binder().bindReadOnly(birthday, Collaborator::birthday);
            binder().bindReadOnly(fullname, Collaborator::fullname);
            binder().bindReadOnly(internalId, Collaborator::internalId);
            binder().bindReadOnly(address, Collaborator::address);
            return form;
        }

        @Override
        protected Collaborator createOrUpdateInstance(Collaborator entity) throws ValidationException {
            return new Collaborator(id.getValue(), oldSsn.getValue(), birthday.getValue(), fullname.getValue(), internalId.getValue(), address.getValue());
        }
    }

    public CollaboratorsView(@NotNull CollaboratorsBoundedDomainUi domain, @NotNull Mode mode) {
        super(Collaborator.class, domain, domain.domain().realm().collaborators(), null, mode);
        form(() -> new CollaboratorForm(this));
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(Collaborator::id).setKey("id").setHeader("SSN").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Collaborator::oldSocialSecurityNumber).setKey("oldId").setHeader("old SSN").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Collaborator::birthday).setKey("birthday").setHeader("Birthday").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Collaborator::fullname).setHeader("Fullname").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Collaborator::internalId).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Collaborator::address).setKey("address").setHeader("Address").setAutoWidth(true).setResizable(true).setSortable(true);
    }
}
