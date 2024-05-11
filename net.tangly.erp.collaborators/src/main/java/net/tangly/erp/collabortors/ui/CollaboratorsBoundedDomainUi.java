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

package net.tangly.erp.collabortors.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.erp.collabortors.services.CollaboratorsBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import org.jetbrains.annotations.NotNull;


public class CollaboratorsBoundedDomainUi extends BoundedDomainUi<CollaboratorsBoundedDomain> {
    private final CollaboratorsView collaboratorsView;
    private final DomainView domainView;

    public CollaboratorsBoundedDomainUi(@NotNull CollaboratorsBoundedDomain domain) {
        super(domain);
        collaboratorsView = new CollaboratorsView(domain);
        domainView = new DomainView(domain);
        currentView(collaboratorsView);

    }

    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Collabborators", e -> select(layout, collaboratorsView));

        addAdministration(layout, menuBar, domainView, new CmdFilesUploadCollaborators(domain()));
        select(layout);
    }

    @Override
    public void refreshViews() {
        collaboratorsView.refresh();
    }
}
