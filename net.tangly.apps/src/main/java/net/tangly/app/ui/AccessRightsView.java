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

package net.tangly.app.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.app.ApplicationView;
import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class AccessRightsView extends ItemView<AccessRights> {
    private static final String USERNAME = "username";
    private static final String ACCESS_RIGHT_CODE = "accessRightCode";
    private static final String USERNAME_LABEL = "Username";
    private static final String ACCESS_RIGHT_CODE_LABEL = "Access Right";

    static class AccessRightsForm extends ItemForm<AccessRights, AccessRightsView> {
        private final TextField username;
        private final TextField domain;
        private final ComboBox<AccessRightsCode> gender;

        public AccessRightsForm(@NotNull AccessRightsView parent) {
            super(parent);
            username = VaadinUtils.createTextField("Username", "username");
            domain = VaadinUtils.createTextField("Domain", "domain");
            gender = ItemForm.createCodeField(CodeType.of(AccessRightsCode.class), "Gender");
            init();
        }

        @Override
        public void create() {
            super.create();
            username.setValue(ApplicationView.username());
        }

        /**
         * Handle the edition or addition of an immutable comment. If the parameter is not null, it is removed from the list before the changed version is added. The updated
         * property values are retrieved from the form. The logic of the item form we inherited from takes care of the provider update to synchronize the user interface grid.
         *
         * @param entity the entity to update or null if it is a created or duplicated instance
         * @return new comment of the entity
         */
        @Override
        protected AccessRights createOrUpdateInstance(AccessRights entity) {
            var rights = new AccessRights(username.getValue(), domain.getValue(), null);
            parent().provider().replace(entity, rights);
            return rights;
        }

        private void init() {
            FormLayout layout = new FormLayout();
            layout.add(username, domain, null);
            form().add(layout, createButtonBar());
            binder().forField(username).bindReadOnly(AccessRights::username);
            binder().forField(domain).bindReadOnly(AccessRights::domain);
        }
    }

    public AccessRightsView(@NotNull BoundedDomainUi<?> domain, @NotNull Mode mode) {
        super(AccessRights.class, domain, ProviderInMemory.of(), null, mode);
        form(() -> new AccessRightsForm(this));
        init();
    }

    private void init() {
        setHeight("15em");
        Grid<AccessRights> grid = grid();
        grid.addColumn(AccessRights::username).setKey(USERNAME).setHeader(USERNAME_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(AccessRights::domain).setKey(DOMAIN).setHeader(DOMAIN_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(AccessRights::right).setKey(ACCESS_RIGHT_CODE).setHeader(ACCESS_RIGHT_CODE_LABEL).setSortable(true).setResizable(
            true).setFlexGrow(0).setWidth("25em");
    }
}
