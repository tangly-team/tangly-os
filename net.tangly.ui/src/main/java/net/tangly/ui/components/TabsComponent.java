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

package net.tangly.ui.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import org.jetbrains.annotations.NotNull;

@Tag("tangly-layout-tabs")
public class TabsComponent extends VerticalLayout {
    private final Map<Tab, Component> tabsToPages;
    private Tabs tabs;
    private Component selectedPage;

    public TabsComponent() {
        tabsToPages = new LinkedHashMap<>();
    }

    public void add(@NotNull Tab tab, @NotNull Component page) {
        tabsToPages.put(tab, page);
        page.setVisible(false);
    }

    public void initialize(@NotNull Tab tab) {
        Div pages = new Div(tabsToPages.values().toArray(new Component[0]));
        pages.setWidthFull();
        pages.setMinWidth("50em");
        tabs = new Tabs(tabsToPages.keySet().toArray(new Tab[0]));
        tabs.setFlexGrowForEnclosedTabs(1);
        tabs.addSelectedChangeListener(event -> {
            selectedPage.setVisible(false);
            selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });
        selectedPage = tabsToPages.get(tab);
        tabs.setSelectedTab(tab);
        selectedPage.setVisible(true);
        add(tabs, pages);
    }

    public Optional<Tab> tabByName(@NotNull String name) {
        return tabsToPages.keySet().stream().filter(e -> name.equals(e.getLabel())).findAny();
    }
}
