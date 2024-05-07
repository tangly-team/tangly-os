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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CmdChangePassword implements Cmd {
    public static final String USERNAME = "Username";
    public static final String CURRENT_PASSWORD = "Current Password";
    public static final String NEW_PASSWORD = "New Password";
    public static final String CONFIRM_PASSWORD = "Confirm New Password";
    public static final String EXECUTE = "Update";
    public static final String CANCEL = "Cancel";

    static class ChangePassword {
        private final Subject subject;
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;

        ChangePassword(Subject subject) {
            this.subject = subject;
        }

        public Subject subject() {
            return subject;
        }

        String oldPassword() {
            return oldPassword;
        }

        void oldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        String newPassword() {
            return newPassword;
        }

        void newPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        String confirmPassword() {
            return confirmPassword;
        }

        void confirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }

    private final CrmBoundedDomain domain;
    private final ChangePassword changePassword;
    private final TextField username;
    private final PasswordField oldPassword;
    private final PasswordField newPassword;
    private final PasswordField confirmPassword;

    private Dialog dialog;

    public CmdChangePassword(@NotNull CrmBoundedDomain domain, @NotNull Subject subject) {
        this.domain = domain;
        changePassword = new ChangePassword(subject);
        username = new TextField(USERNAME, "username");
        username.setReadOnly(true);
        oldPassword = new PasswordField(CURRENT_PASSWORD, "old password");
        oldPassword.setRequired(true);
        newPassword = new PasswordField(NEW_PASSWORD, "new password");
        newPassword.setRequired(true);
        confirmPassword = new PasswordField(CONFIRM_PASSWORD, "new password");
        confirmPassword.setRequired(true);
    }

    @Override
    public void execute() {
        dialog = new Dialog();
        dialog.setWidth("40em");
        FormLayout form = new FormLayout();
        VaadinUtils.set1ResponsiveSteps(form);
        Binder<ChangePassword> binder = new Binder<>();
        binder.bind(username, o -> o.subject().id(), null);
        binder.bind(oldPassword, ChangePassword::oldPassword, ChangePassword::oldPassword);
        binder.bind(newPassword, ChangePassword::newPassword, ChangePassword::newPassword);
        binder.forField(confirmPassword).withValidator(v -> Objects.equals(newPassword.getValue(), v), "new password and confirm password should the same text")
            .bind(ChangePassword::confirmPassword, ChangePassword::confirmPassword);
        binder.readBean(changePassword);
        Button execute = new Button(EXECUTE, VaadinIcon.COGS.create(), e -> {
            try {
                binder.writeBean(changePassword);
                domain.logic().changePassword(changePassword.subject().id(), changePassword.oldPassword(), changePassword.newPassword());
                dialog.close();
                this.dialog = null;

            } catch (ValidationException exception) {
                // TODO
            }
        });
        Button cancel = new Button(CANCEL, e -> dialog.close());
        form.add(username, oldPassword, newPassword, confirmPassword, new HtmlComponent("br"), new HorizontalLayout(execute, cancel));
        dialog.add(form);
        dialog.open();
    }

    @Override
    public Dialog dialog() {
        return dialog;
    }
}
