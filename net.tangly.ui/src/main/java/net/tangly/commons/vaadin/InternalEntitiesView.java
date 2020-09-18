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

import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.providers.Provider;
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
public abstract class InternalEntitiesView<T extends Entity> extends Crud<T> implements CrudForm<T>, HasIdView<T> {
    private static final Logger logger = LoggerFactory.getLogger(InternalEntitiesView.class);
    protected final transient Provider<T> provider;
    protected Binder<T> binder;
    private final TagTypeRegistry registry;

    /**
     * Constructor of the CRUD view for a subclass of entity.
     *
     * @param clazz    class of the entity to display
     * @param mode  mode in which the view should be displayed, the active functions will be accordingly configured
     * @param provider data provider associated with the grid
     * @param registry tag type registry used to configure the tags view of the entity class
     */
    public InternalEntitiesView(@NotNull Class<T> clazz, @NotNull Mode mode, @NotNull Provider<T> provider, TagTypeRegistry registry) {
        super(clazz, mode, DataProvider.ofCollection(provider.items()));
        this.provider = provider;
        this.binder = new Binder<>(clazz);
        this.registry = registry;
    }

    protected void initialize() {
        super.initialize(this, new GridActionsListener<>(provider, grid().getDataProvider(), this::selectedItem));
        VaadinUtils.initialize(grid());
        Grid<T> grid = grid();
        grid.addColumn(Entity::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(Entity::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Entity::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(Entity::fromDate, DateTimeFormatter.ISO_DATE)).setKey("from").setHeader("From").setAutoWidth(true)
                .setResizable(true).setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(Entity::toDate, DateTimeFormatter.ISO_DATE)).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true)
                .setSortable(true);
    }

    @Override
    public FormLayout createForm(@NotNull CrudForm.Operation operation, T entity) {
        TabsComponent tabs = new TabsComponent();
        registerTabs(tabs, of(operation), entity);
        tabs.tabByName("Overview").ifPresent(tabs::initialize);

        FormLayout form = new FormLayout(tabs);
        form.setSizeFull();
        VaadinUtils.setResponsiveSteps(form);
        return form;
    }

    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull Mode mode, T entity) {
        T workedOn = (entity != null) ? entity : create();
        tabs.add(new Tab("Overview"), createOverallView(mode, workedOn));
        tabs.add(new Tab("Comments"), new CommentsView(mode, workedOn));
        tabs.add(new Tab("Tags"), new TagsView(mode, workedOn, registry));
    }

    protected Binder<T> binder() {
        return this.binder;
    }

    protected Provider<T> provider() {
        return this.provider;
    }

    /**
     * factory method to create a new instance of the entity class displayed in the CRUD view. Access to the form allows the use of constructors with
     * parameters.
     *
     * @return a new instance of the entity class
     */
    protected abstract T create();

    /**
     * factory method creating the form layout for the entity simple attributes to be displayed in the overall view.
     *
     * @param mode   mode in which the form will be used
     * @param entity entity under edition
     * @return the form layout
     */
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull T entity) {
        EntityField entityField = new EntityField();
        entityField.setReadOnly(Mode.readOnly(mode));

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField);

        binder = new Binder<>(entityClass());
        binder.readBean(entity);
        return form;
    }

    @Override
    public T formCompleted(Operation operation, T entity) {
        return switch (operation) {
            case UPDATE -> {
                try {
                    binder.writeBean(entity);
                } catch (ValidationException e) {
                    logger.error("validation error", e);
                }
                yield entity;
            }
            case CREATE -> {
                T created = create();
                try {
                    binder.writeBean(created);
                } catch (ValidationException e) {
                    logger.error("validation error", e);
                }
                yield created;
            }
            default -> entity;
        };
    }
}
