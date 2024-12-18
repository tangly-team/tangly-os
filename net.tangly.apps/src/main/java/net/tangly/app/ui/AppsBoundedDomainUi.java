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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.User;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.app.domain.UserManualView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class AppsBoundedDomainUi extends BoundedDomainUi<AppsBoundedDomain> {
    private static final String USERS = "Users";

    public AppsBoundedDomainUi(@NotNull AppsBoundedDomain domain) {
        super(domain);
        addView(User.class, new LazyReference<>(() -> new UsersView(this, Mode.EDITABLE)));
        addView(DomainEntity.class, new LazyReference<>(() -> new DomainView(this)));
        addView(UserManualView.class, new LazyReference<>(() -> new UserManualView(this)));
        currentView(User.class.getSimpleName());
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(USERS, e -> select(layout, view(User.class.getSimpleName()).orElseThrow()));
        subMenu.addItem(USER_MANUAL, _ -> select(layout, view(UserManualView.class).orElseThrow()));
        menuItem = menuBar.addItem(TOOLS);
        subMenu = menuItem.getSubMenu();
        subMenu.addItem(USER_MANUAL, _ -> select(layout, view(UserManualView.class).orElseThrow()));
        if (hasDomainAdminRights() || hasAppAdminRights()) {
            addAdministration(layout, subMenu, view(DomainEntity.class.getSimpleName()).orElseThrow());
        }
        select(layout);
    }
}
