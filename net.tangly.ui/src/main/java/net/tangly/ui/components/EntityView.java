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

package net.tangly.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.value.ValueChangeMode;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <h2>CRUD Operations</h2>
 * <p>The entity view displays a list of entities in a grid. Following operations are available through a pop-up menu:</p>
 * <dl>
 *    <dt>View</dt><dd>Display a read-only form with the entity properties. If the form is displayed and another grid item is selected, the falues of the selected entity are
 *    displayed in the form. </dd>
 *    <dt>Update</dt><dd>Display an editable form with the entity properties. If save operation is selected the updated values are stored into the entity. The grid is updated.</dd>
 *    <dt>Create</dt><dd>Display an editable empty form. If save operation is selected the entity is added to the underlying list and to the grid.</dd>
 *    <dt>Duplicate</dt><dd>Display an editable form with selected properties. If save operation is selected the entity is added to the underlying list and to the grid. The
 *    selected property values are speciffic to the entity type.</dd>
 *    <dt>Delete</dt><dd>Display a read-only form with the entity properties. If delete operation is selected the entity is removed from the underlying list and from the grid.</dd>
 * </dl>
 * <h2>Form Functions</h2>
 * <p>The operations on the entity through the form are:</p>
 * <dl>
 *     <dt>Cancel</dt><dd>Closes the form without any changes in the underlying list or the grid. All changes in the user interface are discarded.</dd>
 *     <dt>Close</dt><dd>Updates the underlying entity with the new field values and closes the form.</dd>
 *     <dt>Fill Form</dt><dd>Fills the form fields with the properties.</dd>
 *     <dt>Clear Form</dt><dd>Clears the form fields.</dd>
 * </dl>
 * <p>The operation on the backend storing the entities are:</p>
 * <dl>
 *     <dt>Create or Update Entity</dt><dd>Creates a new entity or update an existing entity with the field values and stores it in the backend.
 *     The values of the fields are from the user and optionally from an entity being duplicated or updated.</dd>
 *     <dt>Delete Entity</dt><dd>Delete the entity from the backend.</dd>
 * </dl>
 * <p>The entity view contains a grid and a form which both access the same underlying data model and entity provider.</p>
 * <h2>Menu Extensions</h2>
 * <p>Views can add menu options to perform an action on the selected item or on the whole list. A set of related actions are
 * added to the popup menu with a separation. Multiple blocks can be added</p>
 *
 * @param <T> Type of the displayed entities
 */
public abstract class EntityView<T> extends VerticalLayout {
    public enum Mode {
        LIST(Mode.LIST_TEXT), VIEW(Mode.VIEW_TEXT), EDIT(Mode.EDIT_TEXT), CREATE(Mode.CREATE_TEXT), DUPLICATE(Mode.DUPLICATE_TEXT), DELETE(Mode.DELETE_TEXT);

        public static final String LIST_TEXT = "List";
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

    /**
     * Filter to select a subset of entities based on one or more property values of the desired items.
     *
     * @param <T> Type of the entity to filter
     */
    public static abstract class EntityFilter<T> {
        private final GridListDataView<T> dataView;

        protected EntityFilter(@NotNull GridListDataView<T> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public abstract boolean test(@NotNull T entity);

        protected GridListDataView dataView() {
            return dataView;
        }

        protected void refresh() {
            dataView().refreshAll();
        }

        protected static boolean matches(String value, String searchTerm) {
            boolean searchTermUndefined = (searchTerm == null) || (searchTerm.isBlank());
            return searchTermUndefined || ((value == null) || value.toLowerCase().contains(searchTerm.toLowerCase()));
        }
    }

    /**
     * Defines the CRUD contract for a form used to display or modify an entity.
     *
     * @param <T> Type of the entitty manipulaed in the form
     */

    public static abstract class EntityForm<T> {
        protected final EntityView<T> parent;
        private T selectedItem;
        private VerticalLayout formLayout;
        private final Button cancel;
        private final Button action;

        private Mode mode;

        public EntityForm(@NotNull EntityView<T> parent) {
            this.parent = parent;
            formLayout = new VerticalLayout();
            cancel = new Button("Cancel");
            cancel.addClickListener(event -> cancel());
            action = new Button();
            action.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            action.addClickListener(event -> {
                switch (mode) {
                    case VIEW -> cancel();
                    case EDIT, CREATE, DUPLICATE -> updateEntity();
                    case DELETE -> deleteEntity();
                }
            });
        }

        // region CRUD operations available through the popup menu

        /**
         * Display an entity with properties for viewing.
         *
         * @param entity entity to view
         */
        public void display(@NotNull T entity) {
            display(entity, Mode.VIEW);
        }

        /**
         * Display an entity with properties for modification.
         *
         * @param entity entity to modify
         */
        public void edit(@NotNull T entity) {
            display(entity, Mode.EDIT);
        }

        /**
         * Create a new entity form.
         */
        public void create() {
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

        /**
         * Display an entity with properties for deletion.
         *
         * @param entity entity to delete.
         */
        public void delete(@NotNull T entity) {
            display(entity, Mode.DELETE);
        }

        // endregion

        // region Form functions

        /**
         * Cancel the current form operation. All changes are discarded and the form is closed.
         * It is a close operation without propagating any changes.
         */
        void cancel() {
            selectedItem = null;
            clear();
            parent.remove(formLayout);

        }

        /**
         * Display the entity in the form. The form is revealed in the user interface.
         *
         * @param entity entity to display
         * @param mode   display mode of the entity
         */
        protected void display(@NotNull T entity, @NotNull Mode mode) {
            this.mode = mode;
            nameActionButton(mode);
            this.selectedItem = entity;
            clear();
            fillForm(entity);
            parent.add(formLayout);
        }

        /**
         * Fill the form with the properties of the entity.
         *
         * @param entity entity to display in the form
         */
        protected abstract void fillForm(@NotNull T entity);

        /**
         * Clear the content of the form. All property fields are reset to empty or a default value.
         */
        protected abstract void clear();

        protected VerticalLayout form() {
            return formLayout;
        }

        protected Component createButtonsBar() {
            return new HorizontalLayout(cancel, action);
        }

        // endregion

        // region Entity related functions

        /**
         * Update the entity upon modification. THe properties are validated before storing the data.
         * Update means either changing properties of an existing entity or creating a new entity. The creation is used to create a new immutable object.
         *
         * @return the updated entity.
         */
        protected abstract T updateEntity();

        /**
         * Delete the entity from the backend and refreshes the grid.
         *
         * @return
         */
        protected T deleteEntity() {
            T deletedItem = selectedItem();
            if (Objects.nonNull(deletedItem)) {
                parent.provider().delete(deletedItem);
                parent.dataView().refreshAll();
            }
            cancel();
            return deletedItem;
        }

        // endregion

        protected void nameActionButton(@NotNull Mode mode) {
            switch (mode) {
                case VIEW -> {
                    action.setText("Close");
                }
                case EDIT, CREATE, DUPLICATE -> {
                    action.setText("Save");
                }
                case DELETE -> {
                    action.setText("Delete");
                }
            }
        }

        protected T selectedItem() {
            return selectedItem;
        }
    }

    private final Class<T> entityClass;
    private final Provider<T> provider;
    private final boolean hasForm;
    GridContextMenu<T> menu;
    private final Grid<T> grid;
    private final GridListDataView<T> dataView;
    private transient T selectedItem;
    protected EntityForm<T> form;

    public EntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider) {
        this(entityClass, provider, true);
    }

    public EntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider, boolean hasForm) {
        this.entityClass = entityClass;
        this.provider = provider;
        this.hasForm = hasForm;
        grid = new Grid<>();
        dataView = grid.setItems(DataProvider.ofCollection(provider.items()));

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("24em");
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

    protected void initMenu() {
        if (hasForm) {
            menu = grid().addContextMenu();
            menu.addItem(Mode.VIEW_TEXT, event -> form.display(event.getItem().orElse(null)));
            menu.add(new Hr());
            menu.addItem(Mode.EDIT_TEXT, event -> form.edit(event.getItem().orElse(null)));
            menu.addItem(Mode.CREATE_TEXT, event -> form.create());
            menu.addItem(Mode.DUPLICATE_TEXT, event -> form.duplicate(event.getItem().orElse(null)));
            menu.addItem(Mode.DELETE_TEXT, event -> form.delete(event.getItem().orElse(null)));

            SingleSelect<Grid<T>, T> selection = grid.asSingleSelect();
            selection.addValueChangeListener(e -> form.fillForm(e.getValue()));
        }
    }

    /**
     * Add a menu section with entries for custom menu actions. Please use a naming convention to distinguish actions performed on a seclect item and
     * actions performed on the whole list.
     * @param entries menu entries for a section
     */
    protected void addMenuSection(@NotNull List<AbstractMap.SimpleImmutableEntry<String, ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>>>> entries) {
        menu.add(new Hr());
        entries.forEach(e -> menu.addItem(e.getKey(), e.getValue()));
    }

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
