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

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import net.tangly.app.domain.User;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Regular CRUD view on the effort abstraction. The grid and edition dialog are optimized for usability.
 */
@PageTitle("products-efforts")
class UsersView extends ItemView<User> {
    static class UserForm extends ItemForm<User, UsersView> {

        UserForm(@NotNull UsersView parent) {
            super(parent);
            addTabAt("details", details(), 0);

            addTabAt("text", textForm(), 1);
        }

        protected FormLayout details() {
            TextField username = VaadinUtils.createTextField("Username", "username", true, false);
            TextField gravatarEmail = VaadinUtils.createTextField("Gravatar Email", "gravatar email", true, false);
            Checkbox active = new Checkbox("Active");

            FormLayout form = new FormLayout();
            form.add(username, gravatarEmail, active);

            binder().bindReadOnly(username, o -> o.username());
            binder().bindReadOnly(gravatarEmail, o -> o.gravatarEmail());
            binder().bindReadOnly(active, o -> o.active());
            return form;
        }

        @Override
        protected User createOrUpdateInstance(User entity) throws ValidationException {
            return Objects.isNull(entity) ? null : entity;
        }
    }

    public UsersView(@NotNull AppsBoundedDomain domain, @NotNull Mode mode) {
        super(User.class, domain, domain.realm().users(), null, mode);
        form(() -> new UserForm(this));
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(User::username).setKey("username").setHeader("Username").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(User::gravatarEmail).setKey("gravatarEmail").setHeader("Gravatar Email").setAutoWidth(true).setResizable(true)
            .setSortable(true);
    }
}
