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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.StreamResource;
import net.tangly.ui.app.domain.BoundedDomainUi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * The main view of the application is the entry point to the application. Each bounded domain with a user interface is accessible through a tab.
 */
public class ApplicationView extends AppLayout {
    private final String imageName;
    private final Map<String, BoundedDomainUi<?>> boundedDomainUis;
    private Tabs tabs;
    private final MenuBar menuBar;

    public ApplicationView(String imageName) {
        this.imageName = imageName;
        boundedDomainUis = new TreeMap<>();
        setPrimarySection(Section.NAVBAR);
        menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        try {
            byte[] buffer = Thread.currentThread().getContextClassLoader().getResourceAsStream(imageName).readAllBytes();
            Image logo = new Image(new StreamResource(imageName, () -> new ByteArrayInputStream(buffer)), imageName);
            logo.setHeight("44px");
            addToNavbar(new DrawerToggle(), logo, menuBar, menuBar());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

    protected final void selectBoundedDomainUi(String domainName) {
        getBoundedDomainUi(domainName).ifPresent(this::selectBoundedDomainUi);
        IntStream.range(0, tabs.getComponentCount()).mapToObj(i -> tabs.getTabAt(i)).filter(o -> o.getLabel().equals(domainName)).findFirst().ifPresent(tabs::setSelectedTab);
    }

    protected final void selectBoundedDomainUi(Tabs.SelectedChangeEvent event) {
        getBoundedDomainUi(event.getSelectedTab().getLabel()).ifPresent(this::selectBoundedDomainUi);
    }

    protected void selectBoundedDomainUi(BoundedDomainUi<?> ui) {
        menuBar.removeAll();
        ui.select(this, menuBar);
    }

    private MenuBar menuBar() {
        var menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);
        return menuBar;
    }

    protected void drawerMenu() {
        tabs = new Tabs(boundedDomainUis().keySet().stream().map(o -> new Tab(o)).toList().toArray(new Tab[0]));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);
        tabs.addSelectedChangeListener(this::selectBoundedDomainUi);
    }
}