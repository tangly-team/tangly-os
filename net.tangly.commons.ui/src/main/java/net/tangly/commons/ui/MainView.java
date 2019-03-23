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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import net.tangly.commons.models.Comment;
import net.tangly.commons.models.Entity;
import net.tangly.commons.models.EntityImp;
import net.tangly.commons.models.Tag;
import net.tangly.commons.models.TagType;
import net.tangly.commons.models.TagTypeRegistry;

import java.time.LocalDate;

/**
 * The main tagsView contains a button and a click listener.
 */
@PWA(name = "tangly llc ERP", shortName = "tangly-ERP")
@Route("")
@Theme(value = Material.class, variant = Material.LIGHT)
public class MainView extends VerticalLayout {
    private TagsView tagsView;
    private CommentsView commentsView;
    private TagTypeRegistry registry;

    public MainView() {
        registry = new TagTypeRegistry();
        registry.register(TagType.createGeoRegion());
        registry.register(TagType.createGeoLatitude());
        registry.register(TagType.createGeoLongitude());
        registry.register(TagType.createGeoAltitude());
        Entity entity = new EntityImp() {
        };
        entity.id("identifier-001");
        entity.name("name-001");
        entity.fromDate(LocalDate.of(2018, 1, 1));
        entity.toDate(LocalDate.of(2018, 12, 31));
        entity.text("this is a text for entity description");
        entity.add(Tag.of("geo", "region", "CH"));
        entity.add(Comment.of("John Doe", "This is comment 1 written by John Doe"));
        entity.add(Comment.of("John Doe", "This is comment 2 written by John Doe"));
        entity.add(Comment.of("John Doe", "This is comment 3 written by John Doe"));
        EntitiesView view = new EntitiesView(registry, entity);
        add(view);
    }
}
