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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a generic view for entities. The provider is the connection to the application backend. Classes inheriting entities view shall implement at least
 * following methods
 * <ul>
 *     <li>{@link #initializeGrid()} and define the columns of the grid used to display the entities.</li>
 *     <li>{@link #fillForm(Operation, Object, FormLayout)}  and define the fields for the details view of the entity.</li>
 *     <li>{@link #updateOrCreate(Object)} and define the factory method to creat entity instances.</li>
 * </ul>
 * <p>The constructor of the subclass must call the {@link #initializeGrid()} method to cleanly construct an instance.</p>
 *
 * @param <T> type of the external entities
 */
public abstract class EntitiesView<T> extends Crud<T> implements CrudForm<T> {
    protected final Provider<T> provider;

    /**
     * Constructor of the CRUD view for a product.
     *
     * @param entityClass class of the entities displayed
     * @param mode        mode in which the view should be displayed, the active functions will be accordingly configured
     * @param provider    provider of the class
     */
    public EntitiesView(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull Provider<T> provider) {
        super(entityClass, mode, DataProvider.ofCollection(provider.items()));
        super.initialize(this, new GridActionsListener<>(provider, grid().getDataProvider(), this::selectedItem));
        VaadinUtils.initialize(grid());
        this.provider = provider;
    }

    /**
     * Initialize the grid of the view.
     */
    protected abstract void initializeGrid();

    /**
     * Prefills the form layout with application related data and optionally sets the mode such as readonly or enabled of the fields part of the form layout.
     *
     * @param operation operation applied with the form
     * @param entity    optional entity used to prefill the form
     * @param form      form to prefill
     * @return the prefilled form
     */
    protected abstract FormLayout fillForm(@NotNull Operation operation, T entity, FormLayout form);

    /**
     * Updates the entity if the entity is defined otherwise it is a factory method to create a new instance of the entity class displayed in the CRUD view.
     * Access to the form allows the initialization of the instance and the use of constructors with parameters.
     *
     * @param entity entity to update if not null
     * @return the updated instance based on the values in the form fields
     */
    protected abstract T updateOrCreate(T entity);

    @Override
    public FormLayout createForm(@NotNull Operation operation, T entity) {
        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        return fillForm(operation, entity, form);
    }

    @Override
    public T formCompleted(@NotNull Operation operation, T entity) {
        return switch (operation) {
            case UPDATE, CREATE -> updateOrCreate(entity);
            default -> entity;
        };
    }
}
