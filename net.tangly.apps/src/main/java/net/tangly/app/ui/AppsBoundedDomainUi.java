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
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class AppsBoundedDomainUi extends BoundedDomainUi<AppsBoundedDomain> {
    private static final String USERS = "Users";
    private static final String IMPORT_ALL = "Import All";
    private static final String EXPORT_ALL = "Export All";
    private static final String CLEAR_ALL = "Clear All";

    public AppsBoundedDomainUi(@NotNull AppsBoundedDomain domain) {
        super(domain);
        addView(USERS, new LazyReference<>(() -> new UsersView(this, Mode.EDITABLE)));
        addView(ENTITIES, new LazyReference<>(() -> new DomainView(this)));
        currentView(USERS);
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(USERS, e -> select(layout, view(USERS).orElseThrow()));
        addAdministration(layout, menuBar, view(ENTITIES).orElseThrow());
        addHousekeepingMenu(layout, menuBar);
        select(layout);
    }

    // TODO should be moved because a bounded domain should not know his tenant
    public void addHousekeepingMenu(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        if (hasDomainAdminRights()) {
            MenuItem menuItem = menuBar.addItem("Housekeeping");
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem(IMPORT_ALL,
                _ -> executeGlobalAction(() -> domain().tenant().boundedDomains().values().forEach(o -> o.port().importEntities(o))));
            subMenu.addItem(EXPORT_ALL,
                _ -> executeGlobalAction(() -> domain().tenant().boundedDomains().values().forEach(o -> o.port().exportEntities(o))));
            subMenu.addItem(CLEAR_ALL,
                _ -> executeGlobalAction(() -> domain().tenant().boundedDomains().values().forEach(o -> o.port().clearEntities(o))));
        }
    }
}
