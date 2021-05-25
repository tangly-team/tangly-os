/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.core.domain.BoundedDomain;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the interface for the user interface of a bounded domain. The user interface is a set of views to display entities, commands, and dialogs to modify
 * entities.
 */
public interface BoundedDomainUi {
    String ENTITIES = "Entities";
    String ADMINISTRATION = "Administration";
    String ANALYTICS = "Analytics";
    String STATISTICS = "Statistics";
    String IMPORT = "Import";
    String EXPORT = "Export";

    /**
     * Returns the name of bounded domain user interface as displayed in the user interface.
     *
     * @return name of the bounded domain
     */
    String name();

    /**
     * Selects the view to be displayed from the bounded domain user interface and update the menu to reflect the bounded domain.
     *
     * @param layout  layout in which the view will be displayed
     * @param menuBar empty menu bar of the layout
     */
    void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar);

    /**
     * Selects the new current view of the bounded domain interface.
     *
     * @param layout layout to display within
     * @param view   new current view if not null otherwise the current view is refrested
     */
    void select(@NotNull AppLayout layout, Component view);

    default void addAdministration(@NotNull MenuBar menuBar, @NotNull AppLayout layout, @NotNull BoundedDomain<?, ?, ?, ?> domain,
                                   @NotNull DomainView domainView) {
        MenuItem menuItem = menuBar.addItem(ADMINISTRATION);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(STATISTICS, e -> select(layout, domainView));
        subMenu.addItem(IMPORT, e -> {
            new CmdImportEntities(domain, this).execute();
            select(layout, (Component) null);
        });
        subMenu.addItem(EXPORT, e -> new CmdExportEntities(domain).execute());
    }

    default void addAnalytics(@NotNull MenuBar menuBar, @NotNull AppLayout layout, @NotNull AnalyticsView analyticsView) {
        MenuItem menuItem = menuBar.addItem(ANALYTICS);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(ANALYTICS, e -> select(layout, analyticsView));
    }
}
