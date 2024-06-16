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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import net.tangly.commons.lang.Strings;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.User;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;

/**
 * Regular CRUD view on the effort abstraction. The grid and edition dialog are optimized for usability.
 */
@PageTitle("products-efforts")
class UsersView extends ItemView<User> {
    static class UserForm extends ItemForm<User, UsersView> {
        private final Image image;
        private final TextField username;
        private final PasswordField password;
        private final TextField gravatarEmail;
        private final Checkbox active;
        private final TextField naturalPersonId;
        private final One2ManyOwnedField<AccessRights> rights;

        UserForm(@NotNull UsersView parent) {
            super(parent);
            image = new Image();
            image.setWidth(200, Unit.PIXELS);
            image.setHeight(200, Unit.PIXELS);
            username = VaadinUtils.createTextField("Username", "username");
            username.setRequired(true);
            password = new PasswordField("Password");
            gravatarEmail = VaadinUtils.createTextField("Gravatar Email", "gravatar email");
            active = new Checkbox("Active");
            naturalPersonId = VaadinUtils.createTextField("Person Id", "natural person id");
            rights = new One2ManyOwnedField<>(new AccessRightsView(parent.domainUi(), Mode.EDITABLE));
            binder().bindReadOnly(rights, User::accessRights);

            addTabAt("details", details(), 0);
            addTabAt("access rights", rights, 1);
        }

        @Override
        public void value(User value) {
            super.value(value);
            if (Objects.nonNull(value) && Objects.nonNull(value.gravatarEmail())) {
                byte[] photo = User.avatar(value.gravatarEmail());
                image.setSrc(new StreamResource("photo.jpg", () -> new ByteArrayInputStream(photo)));
            }
        }

        @Override
        protected void closeForm() {
            password.setRequired(false);
            password.setEnabled(false);
            super.closeForm();
        }

        @Override
        public void create() {
            super.create();
            password.setEnabled(true);
            password.setRequired(true);
        }

        @Override
        public void duplicate(@NotNull User entity) {
            super.duplicate(entity);
            password.setEnabled(true);
            password.setRequired(true);
        }

        @Override
        protected User createOrUpdateInstance(User entity) throws ValidationException {
            String newPassword = null;
            String newSalt = User.newSalt();
            if (!Strings.isNullOrBlank(password.getValue())) {
                newPassword = User.encryptPassword(password.getValue(), newSalt);
            } else if (entity != null) {
                newPassword = entity.passwordHash();
                newSalt = entity.passwordSalt();
            }
            return new User(username.getValue(), newPassword, newSalt, active.getValue(), naturalPersonId.getValue(),
                List.copyOf(rights.getValue()), gravatarEmail.getValue());
        }

        private FormLayout details() {
            FormLayout form = new FormLayout();
            form.add(username, password, gravatarEmail, active, naturalPersonId);
            form.add(new HtmlComponent("br"));
            form.add(new VerticalLayout(image));
            binder().bindReadOnly(username, o -> o.username());
            binder().bindReadOnly(gravatarEmail, o -> o.gravatarEmail());
            binder().bindReadOnly(active, o -> o.active());
            binder().bindReadOnly(naturalPersonId, o -> o.naturalPersonId());
            return form;
        }

    }

    public UsersView(@NotNull AppsBoundedDomainUi domain, @NotNull Mode mode) {
        super(User.class, domain, domain.domain().realm().users(), null, mode);
        form(() -> new UserForm(this));
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(User::username).setKey("username").setHeader("Username").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(User::gravatarEmail).setKey("gravatarEmail").setHeader("Gravatar Email").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(User::active).setKey("active").setHeader("Active").setAutoWidth(true).setResizable(true).setSortable(true);
    }
}
