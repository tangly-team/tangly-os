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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Define the interface for the visualization of a bounded domain. The user interface is a set of views to display entities, commands, and dialogs to modify entities.
 * <p>Commands can trigger domain changes which should be reflected in the user interface. Some commands update the current displayed view, others update multiple views of the
 * domain interface.</p>
 */
public abstract class BoundedDomainUi<T extends BoundedDomain<?, ?, ?>> {
    public static final String ENTITIES = "Entities";
    public static final String ADMINISTRATION = "Administration";
    public static final String ANALYTICS = "Analytics";
    public static final String STATISTICS = "Statistics";
    public static final String IMPORT = "Import";
    public static final String EXPORT = "Export";
    public static final String CLEAR = "Clear";
    public static final String LOAD = "Load";

    private final T domain;
    private AccessRights rights;
    private LazyReference<?> currentView;
    private Map<String, LazyReference<? extends View>> views;

    public BoundedDomainUi(@NotNull T domain) {
        this.domain = domain;
        views = new HashMap<>();
    }

    /**
     * Return the name of bounded domain user interface as displayed in the user interface.
     *
     * @return name of the bounded domain
     */
    public String name() {
        return domain().name();
    }

    public T domain() {
        return domain;
    }

    public AccessRights rights() {
        return rights;
    }

    public void rights(AccessRights rights) {
        this.rights = rights;
    }

    /**
     * Select the bounded domain and the associated default view to be displayed from the bounded domain user interface and update the menu to reflect the bounded domain.
     *
     * @param layout  layout in which the view will be displayed
     * @param menuBar empty menu bar of the layout. The domain adds domain-specific menu items to the menu bar
     */
    public abstract void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar);

    /**
     * Refresh the views of the bounded domain user interface because the domain entities have changed.
     */
    public void refreshViews() {
        views.values().forEach(view -> view.ifPresent(View::refresh));
    }

    /**
     * Select the new current view of the bounded domain interface.
     *
     * @param layout layout to display within
     * @param view   new current view if not null otherwise the current view is refreshed
     */
    public void select(@NotNull AppLayout layout, LazyReference<?> view) {
        currentView = Objects.isNull(view) ? currentView : view;
        layout.setContent((Component) currentView.get());
    }

    public void select(@NotNull AppLayout layout) {
        layout.setContent(currentView().get());
    }

    public void userChanged(User user) {
        rights = user.accessRightsFor(name()).orElse(null);
        views.values().forEach(view -> view.ifPresent(v -> v.readonly(Objects.nonNull(rights) ? AccessRightsCode.readonlyUser == rights.right() : true)));
    }

    protected final void addView(@NotNull String name, @NotNull LazyReference<? extends View> view) {
        views.put(name, view);
    }

    protected final Optional<LazyReference<? extends View>> view(@NotNull String name) {
        return Optional.ofNullable(views.get(name));
    }

    /**
     * The administration menu provides standard administration operation for a bounded domain.
     * <ul>
     *     <li>Provides statistics about entities of the domain.</li>
     *     <li>Import domain entities from a set of TSV files stored in a directory. The command is responsible for a semantic meaningful ordering of the imports.</li>
     *     <li>Export domain entities to a set of TSV files stored in a directory. Existing files are overwritten. The exported entities could later be imported into the
     *     application with the import domain command.</li>
     *     <li>Up load domain entities through the browser interface. Files are available on client site. The command is optional.</li>
     * </ul>
     */
    protected void addAdministration(@NotNull AppLayout layout, @NotNull MenuBar menuBar, @NotNull LazyReference<?> domainView, Cmd loadDialog) {
        MenuItem menuItem = menuBar.addItem(ADMINISTRATION);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(STATISTICS, _ -> select(layout, domainView));
        var action = subMenu.addItem(IMPORT, _ -> executeGlobalAction(domain.port()::importEntities));
        action.setEnabled(hasDomainAdminRights());
        action = subMenu.addItem(EXPORT, _ -> executeGlobalAction(domain.port()::exportEntities));
        action.setEnabled(hasDomainAdminRights());
        action = subMenu.addItem(CLEAR, _ -> executeGlobalAction(domain.port()::clearEntities));
        action.setEnabled(hasDomainAdminRights());
        if (loadDialog != null) {
            subMenu.addItem(LOAD, _ -> executeGlobalAction(loadDialog::execute));
        }
    }

    protected void addAnalytics(@NotNull AppLayout layout, @NotNull MenuBar menuBar, @NotNull LazyReference<?> analyticsView) {
        MenuItem menuItem = menuBar.addItem(ANALYTICS);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(ANALYTICS, _ -> select(layout, analyticsView));
    }

    private void executeGlobalAction(@NotNull Runnable action) {
        action.run();
        refreshViews();
    }

    protected <V extends View> void currentView(String name) {
        this.currentView = view(name).orElseThrow();
    }

    protected <V extends View> void currentView(LazyReference<V> currentView) {
        this.currentView = currentView;
    }

    protected <V extends Component> LazyReference<V> currentView() {
        return (LazyReference<V>) currentView;
    }

    protected boolean hasReadOnlyRights() {
        return (rights != null) && rights.right() == AccessRightsCode.readonlyUser;
    }
    protected boolean hasDomainAdminRights() {
        return (rights != null) && rights.right() == AccessRightsCode.domainAdmin;
    }
}
