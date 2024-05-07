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

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record CmdLogin(@NotNull CrmBoundedDomain domain) implements Cmd {
    @Override
    public void execute() {
        LoginOverlay component = new LoginOverlay();
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("tangly ERP");
        i18n.getHeader().setDescription("tangly llc ERP Application");
        component.setI18n(i18n);
        component.setOpened(true);
        component.addLoginListener(e -> {
            Optional<Subject> subject = domain.logic().login(e.getUsername(), e.getPassword());
            if (subject.isPresent()) {
                VaadinUtils.setAttribute(component, "subject", subject.get());
                VaadinUtils.setAttribute(component, "username", subject.get().id());
                component.close();
            } else {
                component.setError(true);
            }
        });
        component.addForgotPasswordListener(e -> Notification.show("Please contact tangly llc under info@tangly.net"));
    }
}
