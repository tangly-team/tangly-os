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
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.tangly.app.domain.model.BoundedDomainEntities;
import net.tangly.app.domain.model.BoundedDomainSimpleEntities;
import net.tangly.app.domain.ui.BoundedDomainEntitiesUi;
import net.tangly.app.domain.ui.BoundedDomainSimpleEntitiesUi;
import net.tangly.ui.app.domain.BoundedDomainUi;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PageTitle("tangly Vaadin UI")
@Route("")
public class MainView extends AppLayout {
    private static final String IMAGE_NAME = "tangly70x70.png";
    private final Map<String, BoundedDomainUi> uiDomains;
    private final MenuBar menuBar;

    public MainView() {
        uiDomains = new LinkedHashMap<>();
        add(new BoundedDomainSimpleEntitiesUi(BoundedDomainSimpleEntities.create()));
        add(new BoundedDomainEntitiesUi(BoundedDomainEntities.create()));
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
        uiDomains.get(BoundedDomainSimpleEntities.DOMAIN).select(this, menuBar);
    }

    private static Tabs create(Map<String, BoundedDomainUi> uiDomains) {
        List<Tab> tabs = new ArrayList<>();
        uiDomains.forEach((key, value) -> tabs.add(new Tab(key)));
        return new Tabs(tabs.toArray(new Tab[0]));
    }

    public void add(@NotNull BoundedDomainUi domainUi) {
        uiDomains.put(domainUi.name(), domainUi);
    }

    private MenuBar menuBar() {
        var menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        return menuBar;
    }

    private void drawerMenu() {
        Tabs tabs = create(uiDomains);
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
}
