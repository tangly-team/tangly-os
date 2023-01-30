/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http:www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.app.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.tangly.app.domain.ui.AppBoundedDomainOne;
import net.tangly.app.domain.ui.AppBoundedDomainOneUi;
import net.tangly.app.domain.ui.BoundedDomainUi;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("tangly Vaadin UI")
@Route("")
public class MainView extends AppLayout {
    private static final String IMAGE_NAME = "tangly70x70.png";
    private static final List<String> TAB_NAMES = List.of(AppBoundedDomainOneUi.DOMAIN_NAME);

    private final Map<String, BoundedDomainUi> uiDomains;
    private final MenuBar menuBar;

    public MainView() {
        uiDomains = new HashMap<>();
        put(new AppBoundedDomainOneUi(AppBoundedDomainOne.create()));
        setPrimarySection(Section.NAVBAR);
        menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        try {
            byte[] buffer = Thread.currentThread().getContextClassLoader().getResourceAsStream(IMAGE_NAME).readAllBytes();
            Image image = new Image(new StreamResource(IMAGE_NAME, () -> new ByteArrayInputStream(buffer)), IMAGE_NAME);
            image.setHeight("44px");
            addToNavbar(new DrawerToggle(), image, menuBar, menuBar());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        drawerMenu();
        uiDomains.get(AppBoundedDomainOneUi.DOMAIN_NAME).select(this, menuBar);
    }

    public void put(@NotNull BoundedDomainUi domainUi) {
        uiDomains.put(domainUi.name(), domainUi);
    }

    private MenuBar menuBar() {
        var menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        var admin = menuBar.addItem("Admin");
        return menuBar;
    }

    private void drawerMenu() {
        Tabs tabs = create(TAB_NAMES);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);
        tabs.addSelectedChangeListener(this::selectBoundedDomainUi);
    }

    void selectBoundedDomainUi(Tabs.SelectedChangeEvent event) {
        BoundedDomainUi domainUi = uiDomains.get(event.getSelectedTab().getLabel());
        if (domainUi != null) {
            menuBar.removeAll();
            domainUi.select(this, menuBar);
        }
    }

    private static Tabs create(List<String> tabNames) {
        return new Tabs(tabNames.stream().map(Tab::new).toArray(Tab[]::new));
    }
}
