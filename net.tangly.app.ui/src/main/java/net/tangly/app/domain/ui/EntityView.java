/*
 * Copyright 2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.app.domain.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * THe entity view displays a list of entities in a grid. Following operations are available through a pop-up menu:
 * <dl>
 *    <dt>View</dt><dd>Display a read-only form with the entity properties. If the form is displayed and another grid item is selected, the falues of the selected entity are
 *    displayed in the form. </dd>
 *    <dt>Update</dt><dd>Display an editable form with the entity properties. If save operation is selected the updated values are stored into the entity. The grid is updated.</dd>
 *    <dt>Create</dt><dd>Display an editable empty form. If save operation is selected the entity is added to the underlying list and to the grid.</dd>
 *    <dt>Duplicate</dt><dd>Display an editable form with selected properties. If save operation is selected the entity is added to the underlying list and to the grid. The
 *    selected property values are speciffic to the entity type.</dd>
 *    <dt>Delete</dt><dd>Display a read-only form with the entity properties. If delete operation is selected the entity is removed from the underlying list and from the grid.</dd>
 * </dl>
 * The cancel oeration closes the form without any changes in the underlying list or the grid.
 * <p>The entity view contains a grid and a form which both access the same underlying data model and entity provider.</p>
 *
 * @param <T>
 */
public abstract class EntityView<T> extends VerticalLayout {
    public static enum Mode {
        VIEW(Mode.VIEW_TEXT), EDIT(Mode.EDIT_TEXT), CREATE(Mode.CREATE_TEXT), DUPLICATE(Mode.DUPLICATE_TEXT), DELETE(Mode.DELETE_TEXT);

        public static final String VIEW_TEXT = "View";
        public static final String EDIT_TEXT = "Edit";
        public static final String CREATE_TEXT = "Create";
        public static final String DUPLICATE_TEXT = "Duplicate";
        public static final String DELETE_TEXT = "Delete";

        private final String text;

        Mode(@NotNull String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }

        public boolean readonly() {
            return (this == VIEW) || (this == DELETE);
        }
    }

    public interface EntityFilter<T> {
        boolean test(@NotNull T entity);
    }

    /**
     * Defines the CRUD contract for a form used to display or modify an entity.
     *
     * @param <T> Type of the entitty manipulaed in the form
     */

    public static abstract class EntityForm<T> {
        protected final EntityView<T> parent;
        protected T selectedItem;
        protected VerticalLayout form;

        public EntityForm(@NotNull EntityView<T> parent) {
            this.parent = parent;
        }

        // region CRUD operation

        public void edit(@NotNull T entity) {
            display(entity, Mode.EDIT);
        }

        /**
         * Create a new entity form.
         */
        public void create(@NotNull Mode mode) {
            display(null, Mode.CREATE);
        }

        /**
         * Display and support editing of a duplicated entity.
         *
         * @param entity entity to duplicste
         */
        public void duplicate(@NotNull T entity) {
            display(entity, Mode.DUPLICATE);
        }

        public void delete(@NotNull T entity) {
            display(entity, Mode.DELETE);
        }

        // endregion

        // region form functions

        /**
         * Update the entity upon modification. THe properties are validated before storing the data.
         * Update means either changing properties of an existing entity or creating a new entity. The creation is used to create a new immutable object.
         *
         * @return the updated entity.
         */
        protected abstract T updateEntity();


        /**
         * Display the entity in the form.
         *
         * @param entity entity to display
         * @param mode   display mode of the entity
         */
        protected void display(@NotNull T entity, @NotNull Mode mode) {
            mode(mode);
            displayEntity(entity);
            parent.add(form);
        }

        /**
         * Discard all changes in the form.
         */
        protected void discard() {
            selectedItem = null;
            clear();
            parent.remove(form);
        }

        protected abstract void clear();

        protected Component form() {
            return form;
        }

        // endregion

        // region entity functions

        protected abstract void displayEntity(@NotNull T entity);

        // endregion

        protected abstract void mode(@NotNull Mode mode);

        protected T selectedItem() {
            return selectedItem;
        }

        // endregion
    }

    private final Class<T> entityClass;
    private final Provider<T> provider;
    private final Grid<T> grid;
    private final GridListDataView<T> dataView;
    private transient T selectedItem;
    private transient EntityForm<T> form;

    public EntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider) {
        this.entityClass = entityClass;
        this.provider = provider;
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
        grid.setHeight("30em");
        dataView = grid.setItems(provider.items());
        add(grid);
    }

    protected Provider<T> provider() {
        return provider;
    }

    protected Grid<T> grid() {
        return grid;
    }

    protected GridListDataView<T> dataView() {
        return dataView;
    }

    protected abstract void init();

    protected void addFilter(@NotNull HeaderRow headerRow, @NotNull String key, @NotNull String label, @NotNull Consumer<String> attribute) {
        headerRow.getCell(grid().getColumnByKey(key)).setComponent(createFilterHeader(label, attribute));
    }

    protected static Component createFilterHeader(@NotNull String labelText, @NotNull Consumer<String> filterChangeConsumer) {
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        textField.addValueChangeListener(e -> filterChangeConsumer.accept(e.getValue()));
        return textField;
    }
}
