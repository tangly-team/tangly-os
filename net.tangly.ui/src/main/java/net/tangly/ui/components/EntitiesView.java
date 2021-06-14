/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.components;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.core.providers.Provider;
import net.tangly.ui.grids.GridDecorators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a generic view for entities. The provider is the connection to the application backend. Classes inheriting entities view shall implement at least
 * following methods
 * <ul>
 *     <li>{@link #initialize()} and define the columns of the grid used to display the entities.</li>
 *     <li>{@link #fillForm(Operation, Object, FormLayout)}  and define the fields for the details view of the entity.</li>
 *     <li>{@link #updateOrCreate(Object)} and define the factory method to creat entity instances.</li>
 * </ul>
 * <p>The constructor of the subclass must call the {@link #initialize()} method to cleanly construct an instance.</p>
 *
 * @param <T> type of the external entities
 */
public abstract class EntitiesView<T> extends Crud<T> implements CrudForm<T> {
    public static final String DATE_WIDTH = "8em";
    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    protected final transient Provider<T> provider;

    /**
     * Constructor of the CRUD view for a product.
     *
     * @param entityClass class of the entities displayed
     * @param mode        mode in which the view should be displayed, the active functions will be accordingly configured
     * @param provider    provider of the class
     */
    public EntitiesView(@NotNull Class<T> entityClass, @NotNull Mode mode, @NotNull Provider<T> provider) {
        super(entityClass, mode, DataProvider.ofCollection(provider.items()));
        VaadinUtils.initialize(grid());
        this.provider = provider;
    }

    @Override
    public FormLayout createForm(@NotNull Operation operation, T entity) {
        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        return fillForm(operation, entity, form);
    }

    @Override
    public T formCompleted(@NotNull Operation operation, T entity) {
        return switch (operation) {
            case UPDATE, CREATE -> updateOrCreate(entity);
            default -> entity;
        };
    }

    /**
     * Initialize the grid of the view. The method should be called in the constructor of the subclass to respect the idiom construction is initialization.
     */
    protected abstract void initialize();

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

    protected GridDecorators<T> filterCriteria(boolean hasItemActions, boolean hasGlobalActions, Consumer<GridDecorators<T>> creator) {
        GridDecorators<T> filters = GridDecorators.of(this, grid(), entityClass().getSimpleName(), hasItemActions, hasGlobalActions);
        creator.accept(filters);
        return filters;
    }

    protected GridButtons<T> gridButtons() {
        GridButtons<T> buttons = new GridButtons<>(mode(), this, new GridActionsListener<>(provider, grid().getDataProvider(), this::selectedItem));
        addSelectedItemListener(buttons);
        return buttons;
    }

    protected GridDecorators<T> gridFiltersAndActions(boolean hasItemActions, boolean hasGlobalActions) {
        return GridDecorators.of(this, grid(), entityClass().getSimpleName(), hasItemActions, hasGlobalActions);
    }

    protected static <T> T updateOrCreate(T entity, Binder<T> binder, Supplier<T> factory) {
        T instance = (entity != null) ? entity : factory.get();
        try {
            binder.writeBean(entity);
        } catch (ValidationException e) {
            logger.atError().withThrowable(e).log("Validation error for {}", instance);
        }
        return entity;
    }
}
