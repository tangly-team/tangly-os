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

package net.tangly.app.ui;

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import net.tangly.app.Application;
import net.tangly.app.ApplicationView;
import net.tangly.app.Tenant;
import net.tangly.core.domain.User;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Command to display the login dialog and to authenticate the user.
 * The username is composed of the tenant and the user separated by a slash.
 *
 * @param applicationView the application view owning the command and the login dialog
 */
public record CmdLogin(@NotNull ApplicationView applicationView) implements Cmd {
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
            var credentials = e.getUsername().split("/");
            Tenant tenant = Application.instance().tenant(credentials[0]);
            if (tenant == null) {
                Optional<User> user = tenant.apps().logic().login(credentials[1], e.getPassword());
                if (user.isPresent()) {
                    VaadinUtils.setAttribute(component, ApplicationView.USER, user.get());
                    VaadinUtils.setAttribute(component, ApplicationView.USERNAME, user.get().username());
                    applicationView.userChanged(tenant, user.get());
                    component.close();
                } else {
                    component.setError(true);
                }
            } else {
                component.setError(true);
            }
        });
        component.addForgotPasswordListener(e -> Notification.show("Please contact tangly llc under info@tangly.net"));
    }
}
