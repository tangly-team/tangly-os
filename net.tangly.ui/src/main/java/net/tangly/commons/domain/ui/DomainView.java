/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.domain.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.core.domain.BoundedDomain;

public class DomainView extends VerticalLayout {
    private static final String ENTITIES = "Entities";
    private static final String TAGS = "Tags";
    private final TabsComponent tabs;
    private final BoundedDomain<?, ?, ?, ?> domain;

    public DomainView(BoundedDomain<?, ?, ?, ?> domain) {
        this.domain = domain;
        tabs = new TabsComponent();
        initialize();
    }

    protected void initialize() {
        tabs.add(new Tab(ENTITIES), new DomainEntitiesView(domain));
        tabs.add(new Tab(TAGS), new TagTypesView(domain));
        tabs.initialize(tabs.tabByName(ENTITIES).orElseThrow());
        add(tabs);
    }
}