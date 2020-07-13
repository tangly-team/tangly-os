package net.tangly.commons.vaadin;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import org.jetbrains.annotations.NotNull;

public class TabsComponent extends VerticalLayout {
    private final Map<Tab, Component> tabsToPages;
    private Div pages;
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
        pages = new Div(tabsToPages.values().toArray(new Component[tabsToPages.size()]));
        tabs = new Tabs(tabsToPages.keySet().toArray(new Tab[tabsToPages.size()]));
        tabs.setFlexGrowForEnclosedTabs(1);
        tabs.addSelectedChangeListener(event -> {
            selectedPage.setVisible(false);
            selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });
        selectedPage = tabsToPages.get(tab);
        selectedPage.setVisible(true);
        add(tabs, pages);

    }
}
