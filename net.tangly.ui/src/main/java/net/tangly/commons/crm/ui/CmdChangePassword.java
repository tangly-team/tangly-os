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

package net.tangly.commons.crm.ui;

import java.util.Objects;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.crm.Subject;
import net.tangly.commons.domain.ui.Cmd;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class CmdChangePassword extends Dialog implements Cmd {
    private final CrmBoundedDomain domain;
    private final TextField username;
    private final PasswordField oldPassword;
    private final PasswordField newPassword;
    private final PasswordField confirmPassword;

    public CmdChangePassword(@NotNull CrmBoundedDomain domain) {
        this.domain = domain;
        username = new TextField("Username", "username");
        username.setReadOnly(true);
        oldPassword = new PasswordField("Old Password", "old password");
        oldPassword.setRequired(true);
        newPassword = new PasswordField("New Password", "new password");
        newPassword.setRequired(true);
        confirmPassword = new PasswordField("Confirm Password", "new password");
        confirmPassword.setRequired(true);
    }

    @Override
    public void execute() {
        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        Subject subject = (Subject) VaadinUtils.getAttribute(this, "subject");
        Binder<Subject> binder = new Binder();
        binder.bind(username, Subject::id, null);
        binder.withValidator((item, valueContext) -> {
            if (Objects.equals(newPassword.getValue(), confirmPassword.getValue())) {
                return ValidationResult.ok();
            }
            return ValidationResult.error("Please input the same text in new and confirm password fileds");
        });
        binder.readBean(subject);
        Button execute = new Button("Execute", VaadinIcon.COGS.create(), e -> {
            binder.validate();
            if (binder.isValid()) {
                domain.logic().changePassword(username.getValue(), oldPassword.getValue(), newPassword.getValue());
                this.close();
            }
        });
        Button cancel = new Button("Cancel", e -> this.close());
        form.add(username, oldPassword, newPassword, confirmPassword, new HtmlComponent("br"), new HorizontalLayout(execute, cancel));
        add(form);
        open();

    }
}
