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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.User;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.One2ManyOwnedField;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.util.Objects;

/**
 * Regular CRUD view on the effort abstraction. The grid and edition dialog are optimized for usability.
 */
@PageTitle("products-efforts")
class UsersView extends ItemView<User> {
    static class UserForm extends ItemForm<User, UsersView> {
        private final Image image;
        private final TextField username;
        private final TextField gravatarEmail;
        private final Checkbox active;
        private final TextField naturalPersonId;
        private final One2ManyOwnedField<AccessRights> rights;


        UserForm(@NotNull UsersView parent) {
            super(parent);
            image = new Image();
            image.setWidth(200, Unit.PIXELS);
            image.setHeight(200, Unit.PIXELS);
            username = VaadinUtils.createTextField("Username", "username", true, false);
            gravatarEmail = VaadinUtils.createTextField("Gravatar Email", "gravatar email", true, false);
            active = new Checkbox("Active");
            naturalPersonId = VaadinUtils.createTextField("Person Id", "natural person id", true, false);
            rights = new One2ManyOwnedField<>(new AccessRightsView(parent.domainUi(), parent.domainUi().rights()));
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

        private FormLayout details() {
            FormLayout form = new FormLayout();
            form.add(username, gravatarEmail, active, naturalPersonId);
            form.add(new HtmlComponent("br"));
            form.add(new VerticalLayout(image));
            binder().bindReadOnly(username, o -> o.username());
            binder().bindReadOnly(gravatarEmail, o -> o.gravatarEmail());
            binder().bindReadOnly(active, o -> o.active());
            binder().bindReadOnly(naturalPersonId, o -> o.naturalPersonId());
            return form;
        }

        @Override
        protected User createOrUpdateInstance(User entity) throws ValidationException {
            return Objects.isNull(entity) ? null : entity;
        }
    }

    public UsersView(@NotNull AppsBoundedDomainUi domain, @NotNull AccessRights rights) {
        super(User.class, domain, domain.domain().realm().users(), null, rights);
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
