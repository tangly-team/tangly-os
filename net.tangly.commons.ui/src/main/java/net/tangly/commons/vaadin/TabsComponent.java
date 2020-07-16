package net.tangly.commons.vaadin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import org.jetbrains.annotations.NotNull;

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
        selectedPage.setVisible(true);
        add(tabs, pages);
    }

    public Optional<Tab> tabByName(@NotNull String name) {
        return tabsToPages.keySet().stream().filter(e -> name.equals(e.getLabel())).findAny();
    }
}
