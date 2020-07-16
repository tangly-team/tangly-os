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

package net.tangly.commons.vaadin;

import java.util.function.Consumer;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.core.HasId;
import net.tangly.bus.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a generic view for external entities. External entities have an external unique identifier. The provider is the connection to the application
 * backend.
 *
 * @param <T> type of the external entities
 */
public abstract class ExternalEntitiesView<T extends HasId> extends Crud<T> implements CrudForm<T> {
    protected final Provider<T> provider;
    protected final TextField id;

    /**
     * Constructor of the CRUD view for a product.
     *
     * @param entityClass      class of the entities displayed
     * @param gridConfigurator configurator of the grid view
     * @param provider         provider of the class
     */
    public ExternalEntitiesView(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull Consumer<Grid<T>> gridConfigurator, @NotNull Provider<T> provider) {
        super(entityClass, mode, gridConfigurator, DataProvider.ofCollection(provider.getAll()));
        this.provider = provider;
        initialize(this, new GridActionsListener<>(provider, grid().getDataProvider(), this::selectedItem));
        id = VaadinUtils.createTextField("Id", "id");
    }

    public static <T extends HasId> void defineExternalEntitiesView(@NotNull Grid<T> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(T::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    /**
     * factory method to create a new instance of the entity class displayed in the CRUD view. Access to the form allows the use of constructors with
     * parameters.
     *
     * @return a new instance of the entity class
     */
    protected abstract T create();

    /**
     * Prefills the form layout with application related data and optionally sets the mode such as readonly or enabled of the fields part of the form layout.
     *
     * @param operation operation applied with the form
     * @param entity    optional entity used to prefill the form
     */
    protected abstract FormLayout prefillFrom(@NotNull Operation operation, T entity, FormLayout form);

    @Override
    public FormLayout createForm(Operation operation, T entity) {
        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        switch (operation) {
            case VIEW:
            case UPDATE:
            case DELETE:
                id.setReadOnly(true);
                id.setEnabled(false);
                break;
            case CREATE:
                id.setReadOnly(false);
                id.setEnabled(true);
                break;
        }
        return prefillFrom(operation, entity, form);
    }

    @Override
    public T formCompleted(Operation operation, T entity) {
        switch (operation) {
            case UPDATE:
            case CREATE:
                T created = create();
                return created;
        }
        return entity;
    }
}
