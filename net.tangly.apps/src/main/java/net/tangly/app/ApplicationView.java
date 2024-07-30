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

package net.tangly.app;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.app.ui.AppsBoundedDomainUi;
import net.tangly.app.ui.CmdChangePassword;
import net.tangly.app.ui.CmdLogin;
import net.tangly.app.ui.CmdLogout;
import net.tangly.core.domain.User;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

/**
 * The main view of the application is the entry point to the application. Each bounded domain with a user interface is accessible through a tab.
 * Vaadin creates for each tab a new instance of the bounded domain user interface.
 */
public class ApplicationView extends AppLayout {
    public static final String USERNAME = "username";
    public static final String USER = "user";
    public static String IMAGE_NAME = "icon.png";
    private final Map<String, BoundedDomainUi<?>> boundedDomainUis;
    private Tabs tabs;
    private Tenant tenant;
    private final boolean hasAuthentication;
    private final MenuBar menuBar;


    public ApplicationView(Tenant tenant, boolean hasAuthentication) {
        this.tenant = tenant;
        this.hasAuthentication = hasAuthentication;
        boundedDomainUis = new TreeMap<>();
        setPrimarySection(Section.NAVBAR);
        menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        if (Objects.nonNull(tenant)) {
            ofAppDomainUi();
        }
    }

    /**
     * The method is called when a user successfully logs in. Update access to bounded domains based on the user's access rights.
     * Propagate the user change to all bounded domains.
     *
     * @param user newly logged-in user
     */
    public void userChanged(@NotNull Tenant tenant, @NotNull User user) {
        this.tenant = tenant;
        boundedDomainUis.clear();
        try (var stream = Files.newInputStream(Path.of(tenant.getProperty(Tenant.TENANT_ROOT_DIRECTORY_PROPERTY), IMAGE_NAME))) {
            byte[] buffer = stream.readAllBytes();
            Image logo = new Image(new StreamResource(IMAGE_NAME, () -> new ByteArrayInputStream(buffer)), IMAGE_NAME);
            logo.setHeight("44px");
            addToNavbar(new DrawerToggle(), logo, menuBar);
        } catch (IOException e) {
            addToNavbar(new DrawerToggle(), menuBar);
        }
        ofAppDomainUi();
    }

    public static String username() {
        return (VaadinSession.getCurrent() != null) ? (String) VaadinSession.getCurrent().getAttribute(USERNAME) : null;
    }

    public static User user() {
        return (VaadinSession.getCurrent() != null) ? (User) VaadinSession.getCurrent().getAttribute(USER) : null;
    }

    @Override
    protected void onAttach(@NotNull AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (Objects.isNull(VaadinUtils.getAttribute(this, USER))) {
            new CmdLogin(this).execute();
        }
    }

    @Override
    protected void onDetach(@NotNull DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        boundedDomainUis().values().forEach(BoundedDomainUi::detach);
    }

    public void registerBoundedDomainUi(BoundedDomainUi<?> domain) {
        boundedDomainUis.put(domain.name(), domain);
    }

    public Optional<BoundedDomainUi<?>> getBoundedDomainUi(String name) {
        return Optional.ofNullable(boundedDomainUis.get(name));
    }

    public Map<String, BoundedDomainUi<?>> boundedDomainUis() {
        return Collections.unmodifiableMap(boundedDomainUis);
    }

    protected final void selectBoundedDomainUi(@NotNull String domainName) {
        getBoundedDomainUi(domainName).ifPresent(this::selectBoundedDomainUi);
        domainTab(domainName).ifPresent(tabs::setSelectedTab);
    }

    protected final void selectBoundedDomainUi(Tabs.SelectedChangeEvent event) {
        getBoundedDomainUi(event.getSelectedTab().getLabel()).ifPresent(this::selectBoundedDomainUi);
    }

    protected void selectBoundedDomainUi(@NotNull BoundedDomainUi<?> ui) {
        menuBar.removeAll();
        ui.select(this, menuBar);
        if (hasAuthentication) {
            var menuItem = menuBar.addItem("Account");
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem("Logout", e -> new CmdLogout(this).execute());
            subMenu.addItem("Change Password", e -> new CmdChangePassword(tenant.apps(), user()).execute());
        }
    }

    protected void drawerMenu() {
        tabs = new Tabs(boundedDomainUis().keySet().stream().map(o -> new Tab(o)).toList().toArray(new Tab[0]));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);
        tabs.addSelectedChangeListener(this::selectBoundedDomainUi);
    }

    protected MenuBar menuBar() {
        return menuBar;
    }

    protected Optional<Tab> domainTab(String domain) {
        return Objects.isNull(tabs) ? Optional.empty() :
            IntStream.range(0, tabs.getComponentCount()).mapToObj(i -> tabs.getTabAt(i)).filter(o -> o.getLabel().equals(domain)).findAny();
    }

    private void ofAppDomainUi() {
        registerBoundedDomainUi(new AppsBoundedDomainUi(tenant.apps()));
        domainTab(AppsBoundedDomain.DOMAIN).ifPresent(tab -> tab.setEnabled(hasAuthentication));
    }
}
