/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.EntityImp;
import net.tangly.bus.core.Tag;
import net.tangly.bus.core.TagType;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The entity form provides an interface to all properties of an entity instance. The properties are grouped in tabs.
 * <ul>
 *    <li>The overview panel shows the regular properties of an entity.</li>
 *    <li>The comments panel shows all comments associated with the entity. This panel is a Crud&lt; Comment&gt; instance.</li>
 *    <li>The tags panel shows all tags associated with the entity. This panel is a Crud&lt;Tag&gt; instance.</li>
 * </ul>
 */
public class EntityForm implements CrudForm<Entity>, CrudActionsListener<Entity> {
    private Binder<Entity> binder;
    private final TagTypeRegistry registry = createTagTypeRegistry();
    private final List<Entity> entities = new ArrayList<>(Arrays.asList(create("001"), create("002"), create("003")));

    public static TagTypeRegistry createTagTypeRegistry() {
        TagTypeRegistry registry = new TagTypeRegistry();
        registry.register(TagType.createGeoLatitude());
        registry.register(TagType.createGeoLongitude());
        registry.register(TagType.createGeoAltitude());
        registry.register(TagType.createGeoRegion());
        return registry;
    }

    private static Entity create(String number) {
        Entity entity = new EntityImp() {
        };
        entity.id("identifier-" + number);
        entity.name("name-" + number);
        entity.fromDate(LocalDate.of(2018, Month.JANUARY, 1));
        entity.toDate(LocalDate.of(2018, Month.DECEMBER, 31));
        entity.text("this is a text for entity description");
        entity.add(Tag.of("geo", "region", "CH"));
        entity.add(Comment.of("John Doe", "This is comment 1 written by John Doe for" + number));
        entity.add(Comment.of("John Doe", "This is comment 2 written by John Doe for " + number));
        entity.add(Comment.of("John Doe", "This is comment 3 written by John Doe  for" + number));
        return entity;
    }

    public static void defineGrid(@NotNull Grid<Entity> grid) {
        grid.setVerticalScrollingEnabled(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Entity::oid).setKey("oid").setHeader("Oid").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false)
                .setFrozen(true);
        grid.addColumn(Entity::id).setKey("id").setHeader("Id").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::name).setKey("name").setHeader("Name").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::fromDate).setKey("from").setHeader("From").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::toDate).setKey("to").setHeader("to").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
    }

    public DataProvider<Entity, ?> dataProvider() {
        return new ListDataProvider<>(entities);
    }

    private Component formShown;
    private final Map<Tab, Component> tabsToPages = new HashMap<>();

    @Override
    public FormLayout createForm(Operation operation, Entity entity) {
        Tab overview = new Tab("Overview");
        Component overviewForm = createOverallView(operation, entity);
        tabsToPages.put(overview, overviewForm);

        Tab comments = new Tab("Comments");
        CommentsView commentsView = new CommentsView(entity);
        commentsView.setVisible(false);
        tabsToPages.put(comments, commentsView);

        Tab tags = new Tab("Tags");
        TagsView tagsView = new TagsView(entity, registry);
        tagsView.setVisible(false);
        tabsToPages.put(tags, tagsView);

        Tabs tabs = new Tabs(overview, comments, tags);
        Div pages = new Div(overviewForm, commentsView, tagsView);
        tabs.setFlexGrowForEnclosedTabs(1);
        tabs.setSelectedTab(overview);
        formShown = overviewForm;
        tabs.addSelectedChangeListener(event -> {
            formShown.setVisible(false);
            formShown = tabsToPages.get(tabs.getSelectedTab());
            formShown.setVisible(true);
        });

        FormLayout form = new FormLayout(new VerticalLayout(tabs, pages) {
        });
        form.setSizeFull();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("21em", 2),
                new FormLayout.ResponsiveStep("42em", 3)
        );
        return form;
    }

    TextField oid = CrudForm.createTextField("Oid", "oid", true, true);


    public FormLayout createOverallView(Operation operation, Entity entity) {
        boolean readonly = Operation.isReadOnly(operation);
        TextField id = CrudForm.createTextField("Id", "id", readonly, true);
        TextField name = CrudForm.createTextField("Name", "name", readonly, true);

        DatePicker fromDate = new DatePicker("From Date");
        fromDate.setReadOnly(readonly);

        DatePicker toDate = new DatePicker("To Date");
        toDate.setReadOnly(readonly);

        HtmlComponent br = new HtmlComponent("br");
        TextArea text = new TextArea("Text");
        text.setHeight("8em");
        text.getStyle().set("colspan", "4");
        text.setReadOnly(readonly);

        binder = new Binder<>(Entity.class);
        binder.bind(oid, Entity::id, null);
        binder.bind(id, Entity::id, Entity::id);
        binder.bind(name, Entity::name, Entity::name);
        binder.forField(fromDate)
                .withValidator(from -> (toDate.getValue() == null) || (from.isBefore(toDate.getValue())), "From date must be before to date")
                .bind(Entity::fromDate, Entity::fromDate);
        binder.forField(toDate)
                .withValidator(to -> (fromDate.getValue() == null) || (to.isAfter(fromDate.getValue())), "To date must be after from date")
                .bind(Entity::toDate, Entity::toDate);
        binder.bind(text, Entity::text, Entity::text);

        FormLayout form = new FormLayout(oid, id, name, fromDate, toDate, br, text);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("21em", 2),
                new FormLayout.ResponsiveStep("42em", 3)
        );
        binder.readBean(entity);
        return form;
    }

    @Override
    public Entity formCompleted(Operation operation, Entity entity) {
        switch (operation) {
            case UPDATE:
                try {
                    binder.writeBean(entity);
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
                break;
            case CREATE:
                try {
                    Entity created = new EntityImp() {
                    };
                    binder.writeBean(created);
                    return created;
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
                break;
        }
        return entity;
    }

    @Override
    public void entityAdded(DataProvider<Entity, ?> dataProvider, Entity entity) {
        entities.add(entity);
    }

    @Override
    public void entityDeleted(DataProvider<Entity, ?> dataProvider, Entity entity) {
        entities.remove(entity);
    }
}
