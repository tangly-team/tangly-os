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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entity form provides an interface to all properties of an entity instance. The properties are grouped in tabs.
 * <ul>
 *    <li>The overview panel shows the regular properties of an entity.</li>
 *    <li>The comments panel shows all comments associated with the entity. This panel is a Crud&lt; Comment&gt; instance.</li>
 *    <li>The tags panel shows all tags associated with the entity. This panel is a Crud&lt;Tag&gt; instance.</li>
 * </ul>
 */
public abstract class EntitiesView<T extends Entity> extends Crud<T> implements CrudForm<T>, CrudActionsListener<T> {
    private static final Logger logger = LoggerFactory.getLogger(EntitiesView.class);
    private final transient List<T> items;
    private Binder<T> binder;
    private final TagTypeRegistry registry;

    /**
     * Constructor of the CRUD view for a subclass of entity.
     *
     * @param clazz            class of the entity to display
     * @param gridConfigurator configurator for the grid displaying a list of entities
     * @param items            items displayed in the grid
     * @param provider         data provider associated with the grid
     * @param registry         tag type registry used to configure the tags view of the entity class
     */
    public EntitiesView(@NotNull Class<T> clazz, @NotNull Consumer<Grid<T>> gridConfigurator, @NotNull List<T> items, DataProvider<T, ?> provider,
                        TagTypeRegistry registry) {
        super(clazz, Mode.EDITABLE, gridConfigurator, provider);
        this.items = new ArrayList<>(items);
        this.registry = registry;
        initialize(this, this);
    }

    public static <E extends Entity> void defineGrid(@NotNull Grid<E> grid) {
        Crud.initialize(grid);
        grid.addColumn(Entity::oid).setKey("oid").setHeader("Oid").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false)
                .setFrozen(true);
        grid.addColumn(Entity::id).setKey("id").setHeader("Id").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::name).setKey("name").setHeader("Name").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::fromDate).setKey("from").setHeader("From").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::toDate).setKey("to").setHeader("To").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
    }

    private Component formShown;
    private final Map<Tab, Component> tabsToPages = new HashMap<>();

    @Override
    public FormLayout createForm(Operation operation, T entity) {
        TabsComponent tabs = new TabsComponent();
        Tab overview = new Tab("Overview");
        tabs.add(overview, createOverallView(operation, entity));
        tabs.add(new Tab("Comments"), new CommentsView(entity));
        tabs.add(new Tab("Tags"), new TagsView(entity, registry));
        tabs.initialize(overview);

        FormLayout form = new FormLayout(tabs);
        form.setSizeFull();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("21em", 2),
                new FormLayout.ResponsiveStep("42em", 3)
        );
        return form;
    }

    private FormLayout createOverallView(Operation operation, T entity) {
        boolean readonly = Operation.isReadOnly(operation);
        TextField oid = CrudForm.createTextField("Oid", "oid", true, true);
        TextField id = CrudForm.createTextField("Id", "id", readonly, true);
        TextField name = CrudForm.createTextField("Name", "name", readonly, true);

        DatePicker fromDate = new DatePicker("From Date");
        fromDate.setReadOnly(readonly);

        DatePicker toDate = new DatePicker("To Date");
        toDate.setReadOnly(readonly);

        TextArea text = new TextArea("Text");
        text.setHeight("8em");
        text.setReadOnly(readonly);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("25em", 1), new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3)
        );
        form.add(oid, id, name, fromDate, toDate, new HtmlComponent("br"), text);
        form.setColspan(text, 3);

        binder = new Binder<T>(entityClass());
        binder.bind(oid, o -> Long.toString(o.oid()), null);
        binder.bind(id, Entity::id, Entity::id);
        binder.bind(name, Entity::name, Entity::name);
        binder.forField(fromDate)
                .withValidator(from -> (toDate.getValue() == null) || (from.isBefore(toDate.getValue())), "From date must be before to date")
                .bind(Entity::fromDate, Entity::fromDate);
        binder.forField(toDate)
                .withValidator(to -> (fromDate.getValue() == null) || (to.isAfter(fromDate.getValue())), "To date must be after from date")
                .bind(Entity::toDate, Entity::toDate);
        binder.bind(text, Entity::text, Entity::text);
        binder.readBean(entity);
        return form;
    }

    @Override
    public T formCompleted(Operation operation, T entity) {
        switch (operation) {
            case UPDATE:
                try {
                    binder.writeBean(entity);
                } catch (ValidationException e) {
                    logger.error("validation error", e);
                }
                break;
            case CREATE:
                try {
                    T created = create();
                    binder.writeBean(created);
                    return created;
                } catch (ValidationException e) {
                    logger.error("validation error", e);
                }
                break;
        }
        return entity;
    }

    @Override
    public void entityAdded(DataProvider<T, ?> provider, T entity) {
        items.add(entity);
        CrudActionsListener.super.entityAdded(provider, entity);
    }

    @Override
    public void entityDeleted(DataProvider<T, ?> provider, T entity) {
        items.remove(entity);
        CrudActionsListener.super.entityAdded(provider, entity);
    }

    /**
     * factory method to create a new instance of the entity class displayed in the CRUD view. Access to the form allows the use of constructors with
     * parameters.
     *
     * @return a new instance of the entity class
     */
    protected abstract T create();
}
