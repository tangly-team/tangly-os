/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import net.tangly.commons.models.Entity;
import net.tangly.commons.models.TagTypeRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * The entity view is a tabbed view with tabs for details of the entity, comments pane, and tags pane. Additional panes can be provided for 1..n
 * relations or specific details groups.
 */
public class EntitiesView extends Composite<Div> {
    private transient Entity entity;
    private Map<Tab, Component> tabsToPages = new HashMap<>();
    private Component selectedPage;
    private CommentsView commentsView;
    private Tabs tabs;

    public EntitiesView(TagTypeRegistry registry, Entity entity) {
        tabs = new Tabs();
        Tab detailsTab = new Tab("Details");
        Component detailsPage = new EntityView(entity);
        detailsPage.setVisible(true);
        Tab commentsTab = new Tab("Comments");
        Component commentPage = new CommentsView(entity);
        commentPage.setVisible(false);
        Tab tagsTab = new Tab("Tags");
        Component tagsPage = new TagsView(registry, entity);
        tagsPage.setVisible(false);
        tabsToPages.put(detailsTab, detailsPage);
        tabsToPages.put(commentsTab, commentPage);
        tabsToPages.put(tagsTab, tagsPage);
        tabs = new Tabs(detailsTab, commentsTab, tagsTab);
        Div pages = new Div(detailsPage, commentPage, tagsPage);
        selectedPage = detailsPage;
        tabs.setSelectedTab(detailsTab);
        tabs.addSelectedChangeListener(event -> {
            if (selectedPage != null) {
                selectedPage.setVisible(false);
            }
            selectedPage = tabsToPages.get(tabs.getSelectedTab());
            if (selectedPage != null) {
                selectedPage.setVisible(true);
            }
        });
        getContent().add(tabs, pages);
    }
}
