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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.User;
import net.tangly.core.events.EntityChangedInternalEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;

/**
 * Define the interface for the visualization of a bounded domain. The user interface is a set of views to display entities, commands, and dialogs to modify entities.
 * <p>Commands can trigger domain changes which should be reflected in the user interface. Some commands update the current displayed view, others update multiple views of the
 * domain interface.</p>
 */
public abstract class BoundedDomainUi<T extends BoundedDomain<?, ?, ?>> implements BoundedDomain.EventListener {
    public static final String ADMINISTRATION = "Administration";
    public static final String ANALYTICS = "Analytics";
    public static final String CLEAR = "Clear";
    public static final String DOCUMENTS = "Documents";
    public static final String ENTITIES = "Entities";
    public static final String EXPORT = "Export";
    public static final String IMPORT = "Import";
    public static final String STATISTICS = "Statistics";

    private final T domain;
    private AccessRights rights;
    private LazyReference<?> currentView;
    private final Map<Class<?>, LazyReference<? extends View>> views;
    private Flow.Subscription subscription;

    public BoundedDomainUi(@NotNull T domain) {
        this.domain = domain;
        views = new HashMap<>();
    }

    public static String username() {
        return (VaadinSession.getCurrent() != null) ? (String) VaadinSession.getCurrent().getAttribute("username") : null;
    }

    public static User user() {
        return (VaadinSession.getCurrent() != null) ? (User) VaadinSession.getCurrent().getAttribute("user") : null;
    }


    /**
     * Returns the name of bounded domain user interface as displayed in the user interface.
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

    /**
     * Selects the bounded domain and the associated default view to be displayed from the bounded domain user interface and update the menu to reflect the
     * bounded domain.
     *
     * @param layout  layout in which the view will be displayed
     * @param menuBar empty menu bar of the layout. The domain adds domain-specific menu items to the menu bar
     */
    public abstract void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar);

    /**
     * Refreshes the views of the bounded domain user interface because the domain entities have changed.
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

    public void userChanged(@NotNull User user) {
        rights = user.accessRightsFor(name()).orElse(null);
        views.values().forEach(view -> view.ifPresent(v -> v.readonly(Objects.nonNull(rights) ? AccessRightsCode.readonlyUser == rights.right() : true)));
    }

    public void detach() {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    // region EventListener

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(@NotNull Object event) {
        if (event instanceof EntityChangedInternalEvent entityChanged) {
            view(entityChanged.entityName()).ifPresent(v -> UI.getCurrent().access(() -> v.ifPresent(View::refresh)));
        }
    }

    // endregion

    protected final void addView(@NotNull Class<?> clazz, @NotNull LazyReference<? extends View> view) {
        views.put(clazz, view);
    }

    protected final Optional<LazyReference<? extends View>> view(@NotNull Class<?> clazz) {
        return Optional.ofNullable(views.get(clazz));
    }

    protected final Optional<LazyReference<? extends View>> view(@NotNull String name) {
        Class<?> key = views.keySet().stream().filter(o -> o.getSimpleName().equals(name)).findAny().orElse(null);
        return Objects.nonNull(key) ? Optional.ofNullable(views.get(key)) : Optional.empty();
    }

    /**
     * The administration menu provides standard administration operation for a bounded domain.
     * <ul>
     *     <li>Provides statistics about entities of the domain.</li>
     *     <li>Import domain entities from a set of TSV files stored in a directory. The command is responsible for a semantic meaningful ordering of the imports.</li>
     *     <li>Export domain entities to a set of TSV files stored in a directory. Existing files are overwritten. The exported entities could later be imported into the
     *     application with the import domain command.</li>
     * </ul>
     */
    protected void addAdministration(@NotNull AppLayout layout, @NotNull MenuBar menuBar, @NotNull LazyReference<?> domainView) {
        if (hasDomainAdminRights() || hasAppAdminRights()) {
            MenuItem menuItem = menuBar.addItem(ADMINISTRATION);
            SubMenu subMenu = menuItem.getSubMenu();
            var action = subMenu.addItem(STATISTICS, _ -> select(layout, domainView));

            subMenu.addSeparator();
            action.setEnabled(hasDomainAdminRights());
            action = subMenu.addItem(IMPORT, _ -> executeGlobalAction(() -> domain.port().importEntities(domain())));
            action.setEnabled(hasDomainAdminRights());
            action = subMenu.addItem(EXPORT, _ -> executeGlobalAction(() -> domain.port().exportEntities(domain())));
            action.setEnabled(hasDomainAdminRights());
            action = subMenu.addItem(CLEAR, _ -> executeGlobalAction(() -> domain.port().clearEntities(domain())));
            action.setEnabled(hasDomainAdminRights());

            subMenu.addSeparator();
            action = subMenu.addItem("Import All", _ -> domain().directory().boundedDomains().forEach(o -> o.port().importEntities(o)));
            action.setEnabled(hasAppAdminRights());
            action = subMenu.addItem("Export All", _ -> domain().directory().boundedDomains().forEach(o -> o.port().exportEntities(o)));
            action.setEnabled(hasAppAdminRights());
            action = subMenu.addItem("Clear All", _ -> domain().directory().boundedDomains().forEach(o -> o.port().clearEntities(o)));
            action.setEnabled(hasAppAdminRights());
        }
    }

    protected void addAnalytics(@NotNull AppLayout layout, @NotNull MenuBar menuBar, @NotNull LazyReference<?> analyticsView) {
        MenuItem menuItem = menuBar.addItem(ANALYTICS);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(ANALYTICS, _ -> select(layout, analyticsView));
    }

    protected final void executeGlobalAction(@NotNull Runnable action) {
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
        return (rights != null) && ((rights.right() == AccessRightsCode.domainAdmin) || (rights.right() == AccessRightsCode.appAdmin));
    }

    protected boolean hasAppAdminRights() {
        return (Objects.nonNull(user()) && user().accessRights().stream().anyMatch(o -> o.right() == AccessRightsCode.appAdmin));
    }
}
