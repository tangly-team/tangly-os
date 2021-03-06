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
import java.util.Objects;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import net.tangly.components.grids.GridDecorators;
import net.tangly.core.Entity;
import net.tangly.core.QualifiedEntity;
import net.tangly.core.TypeRegistry;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * The entity form provides an interface to all fields of an entity instance. The fields are grouped in tabs.
 * <ul>
 *    <li>The overview panel shows the regular fields of an entity.</li>
 *    <li>The comments panel shows all comments associated with the entity. This panel is a Crud&lt; Comment&gt; instance.</li>
 *    <li>The tags panel shows all tags associated with the entity. This panel is a Crud&lt;Tag&gt; instance.</li>
 * </ul>
 */
public abstract class InternalEntitiesView<T extends Entity> extends EntitiesView<T> implements CrudForm<T>, HasIdView<T> {
    public static final String OID_WIDTH = "5em";
    public static final String ID_WIDTH = "12em";
    public static final String NAME_WIDTH = "20em";
    protected Binder<T> binder;
    private final TypeRegistry registry;

    /**
     * Constructor of the CRUD view for a subclass of entity.
     *
     * @param clazz    class of the entity to display
     * @param mode     mode in which the view should be displayed, the active functions will be accordingly configured
     * @param provider data provider associated with the grid
     * @param registry tag type registry used to configure the tags view of the entity class
     */
    public InternalEntitiesView(@NotNull Class<T> clazz, @NotNull Mode mode, @NotNull Provider<T> provider, TypeRegistry registry) {
        super(clazz, mode, provider);
        this.binder = new Binder<>(clazz);
        this.registry = registry;
    }

    protected static <T extends Entity> void addEntityFilters(GridDecorators<T> filters) {
        filters.addFilter(new GridDecorators.FilterText<>(filters, T::name, "Name", "name")).addFilter(new GridDecorators.FilterInterval<>(filters))
            .addFilter(new GridDecorators.FilterTags<>(filters));
    }

    protected static <T extends QualifiedEntity> void addQualifiedEntityFilters(GridDecorators<T> filters) {
        filters.addFilter(new GridDecorators.FilterText<>(filters, T::id, "Id", "id"));
        addEntityFilters(filters);
    }

    protected static <T extends QualifiedEntity> void addQualifiedEntityColumns(Grid<T> grid) {
        grid.addColumn(QualifiedEntity::oid).setKey("oid").setHeader("Oid").setResizable(true).setSortable(true).setFlexGrow(0).setWidth(OID_WIDTH);
        grid.addColumn(QualifiedEntity::id).setKey("id").setHeader("Id").setResizable(true).setSortable(true).setFlexGrow(0).setWidth(ID_WIDTH);
        grid.addColumn(QualifiedEntity::name).setKey("name").setHeader("Name").setResizable(true).setSortable(true).setFlexGrow(0).setWidth(NAME_WIDTH);
        grid.addColumn(new LocalDateRenderer<>(QualifiedEntity::fromDate, DateTimeFormatter.ISO_DATE)).setKey("from").setHeader("From").setResizable(true)
            .setSortable(true).setFlexGrow(0).setWidth(DATE_WIDTH);
        grid.addColumn(new LocalDateRenderer<>(QualifiedEntity::toDate, DateTimeFormatter.ISO_DATE)).setKey("to").setHeader("To").setResizable(true)
            .setSortable(true).setFlexGrow(0).setWidth(DATE_WIDTH);
    }

    @Override
    public FormLayout fillForm(@NotNull Operation operation, T entity, FormLayout form) {
        if (Objects.nonNull(entity)) {
            TabsComponent tabs = new TabsComponent();
            registerTabs(tabs, of(operation), entity);
            tabs.tabByName("Overview").ifPresent(tabs::initialize);
            form.add(tabs);
            form.setSizeFull();
        } else {
            form.add(createOverallView(of(operation), entity));
        }
        return form;
    }

    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull Mode mode, @NotNull T entity) {
        tabs.add(new Tab("Overview"), createOverallView(mode, entity));
        tabs.add(new Tab("Comments"), new CommentsView(mode, entity));
        tabs.add(new Tab("Tags"), new TagsView(mode, entity, registry));
    }

    protected Binder<T> binder() {
        return this.binder;
    }

    protected Provider<T> provider() {
        return this.provider;
    }

    /**
     * Factory method creating the form layout for the entity simple attributes to be displayed in the overall view.
     *
     * @param mode   mode in which the form will be used
     * @param entity entity under edition
     * @return the form layout
     */
    protected FormLayout createOverallView(@NotNull Mode mode, T entity) {
        EntityField<T> entityField = new EntityField<>();
        entityField.setReadOnly(mode.readOnly());

        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        entityField.addEntityComponentsTo(form);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        if (Objects.nonNull(entity)) {
            binder.readBean(entity);
        }
        return form;
    }
}
