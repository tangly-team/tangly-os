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

package net.tangly.erp.collaborators.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.domain.DomainEntity;
import net.tangly.erp.collaborators.domain.Collaborator;
import net.tangly.erp.collabortors.services.CollaboratorsBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;


public class CollaboratorsBoundedDomainUi extends BoundedDomainUi<CollaboratorsBoundedDomain> {
    private static final String COLLABORATORS = "Collaborators";

    public CollaboratorsBoundedDomainUi(@NotNull CollaboratorsBoundedDomain domain) {
        super(domain);
        addView(Collaborator.class, new LazyReference<>(() -> new CollaboratorsView(this, Mode.EDITABLE)));
        addView(DomainEntity.class, new LazyReference<>(() -> new DomainView(this)));
        currentView(view(Collaborator.class).orElseThrow());

    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(COLLABORATORS, _ -> select(layout, view(Collaborator.class).orElseThrow()));
        if (hasDomainAdminRights() || hasAppAdminRights()) {
            menuItem = menuBar.addItem(TOOLS);
            subMenu = menuItem.getSubMenu();
            addAdministration(layout, subMenu, view(DomainEntity.class).orElseThrow());
        }
        select(layout);
    }
}
