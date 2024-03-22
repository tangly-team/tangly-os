/*
 * Copyright 2023-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines the CRUD contract for a form used to display or modify an entity. The abstract methods are:
 * <dl>
 *    <dt>{@link #mode(Mode mode)}</dt><dd>Set the mode of the fields in the form based on selected CRUD operation and additional business Logic.</dd>
 *    <dt>{@link #value(Object)}</dt><dd>Fill the form with properties of the entity and business logic.</dd>
 *    <dt>{@link #clear()}</dt><dd>Clear all fields in the form</dd>
 *    <dt>{@link #createOrUpdateInstance(Object)}</dt><dd>Create a new entity based on the fields and business logic.</dd>
 * </dl>
 * <p>A form has a binder to transfer values between the form fields and the entity. The form has also a reference to the view containing the entities, which could be displayed
 * in the form.</p>
 *
 * <h2>Mode Propagation</h2>
 * <p>An item form can contains multiple item views. When the form is open with a specific operation, All views in the form should have the same mode for consistency.
 * If a user opens the form with a view command, all contained components should also set to the mode view and be read-only.
 * The developer is responsible to overwrite the {@link ItemForm#mode} method to propagate the mode change to all contained components.
 * The rules should be:</p>
 * <dl>
 *     <dt>LIST</dt><dd>shall be propagated as LIST.</dd>
 *     <dt>VIEW, DELETE</dt><dd>shall be propagated or translated to VIEW.</dd>
 *     <dt>EDIT, CREATE, DUPLICATE</dt><dd>shall be translated to EDIT.</dd>
 * </dl>
 *
 * <h2>Buttons</h2>
 * <dl>
 *     <dt>Cancel</dt><dd>The cancel button cancels the operation without any changes. The button shortcut is <i>ESC</i>.</dd>
 *     <dt>Action</dt><dd>The action button executes the operation. The label is dependant on the operation. The button shortcut is <i>ENTER</i>.</dd>
 * </dl>
 *
 * @param <T> Type of the entity manipulated in the form
 */
public abstract class ItemForm<T, U extends ItemView<T>> {
    private static final Logger logger = LogManager.getLogger();
    private final U parent;
    private final Binder<T> binder;
    private final VerticalLayout formLayout;
    private final TabSheet tabSheet;

    private final Button cancel;
    private final Button action;
    private T value;
    private Mode mode;
    private Dialog dialog;

    protected ItemForm(@NotNull U parent) {
        this.parent = parent;
        binder = new Binder<>(parent.entityClass());
        mode = Mode.VIEW;

        formLayout = new VerticalLayout();
        tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_SMALL);

        cancel = new Button("Cancel");
        cancel.addClickListener(event -> closeForm());
        cancel.addClickShortcut(Key.ESCAPE);
        action = new Button();
        action.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        action.addClickListener(event -> {
            switch (mode) {
                case EDIT, CREATE, DUPLICATE -> updateEntity();
                case DELETE -> deleteEntity();
            }
            closeForm();
        });
        form().add(tabSheet, createButtonBar());
    }

    public static <T extends Code> ComboBox<T> createCodeField(@NotNull CodeType<T> codeType, @NotNull String label) {
        ComboBox<T> codeField = new ComboBox<>(label);
        codeField.setItemLabelGenerator(o -> (Objects.isNull(o) ? "" : o.code()));
        codeField.setItems(codeType.codes());
        codeField.setPlaceholder("select item");
        codeField.setOpened(false);
        return codeField;
    }

    /**
     * Return the mode of the form. All contained items should have the same mode for consistency.
     *
     * @return the mode of the form
     * @see #mode(Mode)
     */
    public Mode mode() {
        return mode;
    }

    /**
     * Set the mode of the form and propagate it to all components registered in the binder. Subclasses should overwrite the method to ensure that the mode is propagated to all
     * contained views.
     *
     * @param mode mode of the form
     * @see #mode()
     */
    public void mode(@NotNull Mode mode) {
        this.mode = mode;
        binder.getFields().forEach(o -> o.setReadOnly(mode.readonly()));
    }

    /**
     * Return the value displayed in the form.
     *
     * @return value displayed in the form or null.
     */
    public T value() {
        return value;
    }

    /**
     * Set the value displayed in the form. If the value is null, the form fields are reset to empty values.
     * <p>Overwrite the method to update components not supported by Vaadin binder for example an image. Handle null value accordingly. Do not forget to call the overwritten
     * method to trigger the binding mechanism.</p>
     *
     * @param value value to display in the form
     */
    public void value(T value) {
        this.value = value;
        if (Objects.nonNull(value)) {
            binder().readBean(value);
        } else {
            clear();
        }
    }

    protected Binder<T> binder() {
        return binder;
    }

    protected U parent() {
        return parent;
    }

    protected Class<T> entityClass() {
        return parent().entityClass();
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
     * @param entity entity to duplicate
     */
    public void duplicate(@NotNull T entity) {
        display(entity, Mode.DUPLICATE);
        this.value = null;
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
     * Display the value in the form. The form is revealed in the user interface. The new mode is used to propagate the mode and configure the action buttons.
     *
     * @param value value to display
     * @param mode  display mode of the value
     */
    private void display(T value, @NotNull Mode mode) {
        nameActionButton(mode);
        value(value);
        if (parent.isFormEmbedded()) {
            parent.add(formLayout);
        } else {
            dialog = VaadinUtils.createDialog();
            dialog.add(formLayout);
            dialog.open();
        }
    }

    private void closeForm() {
        if (Objects.nonNull(dialog)) {
            dialog.close();
            dialog = null;
        } else {
            parent.remove(formLayout);
        }
        value(null);
        clear();
    }

    /**
     * Clear the content of the form. All property fields are reset to empty or a default value. The default implementation clears all fields registered in the binder. Custom
     * fields are cleared as regular fields because both implements <i>HasValue</i> and therefore are eligble for the binder.
     */
    protected void clear() {
        binder.getFields().forEach(HasValue::clear);
    }

    /**
     * Create the form containing all the fields to display an entity. The form is added into the CRUD form with the associated operations.
     *
     * @return the vertical layout containing all fields to display the entity
     */
    protected VerticalLayout form() {
        return formLayout;
    }

    /**
     * Create the CRUD button bar. The button bar has always a cancel operation and an additional CRUD operation.
     *
     * @return component containing the CRUD form command button
     */
    protected Component createButtonBar() {
        return new HorizontalLayout(cancel, action);
    }

    // endregion

    // region Entity related functions

    protected abstract void init();

    /**
     * Create or update the instance with the new values from the user interface.
     *
     * @param entity the entity to update or null if it is a created or duplicated instance
     * @return new or updated instance
     */
    protected abstract T createOrUpdateInstance(T entity) throws ValidationException;

    /**
     * Update the entity upon modification. The properties are validated before storing the data. Update means either changing properties of an existing entity or creating a new
     * entity. The creation is used to create a new immutable object. If a new instance was created, the old one is deleted.
     * <p>The {@link ItemForm#createOrUpdateInstance(Object)} is responsible to extract the updated data from the user interface.</p>
     *
     * @return the updated entity.
     */
    public T updateEntity() throws RuntimeException {
        try {
            T entity = createOrUpdateInstance(value());
            if (Objects.nonNull(value()) && !Objects.equals(entity, value())) {
                parent.provider().delete(value());
            }
            parent.provider().update(entity);
            parent.dataView().refreshAll();
            return entity;
        } catch (ValidationException e) {
            logger.atError().log(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete the entity from the backend and refreshes the grid.
     *
     * @return deleted entity instance
     */
    protected T deleteEntity() {
        T deletedItem = value();
        if (Objects.nonNull(deletedItem)) {
            parent.provider().delete(deletedItem);
            parent.dataView().refreshAll();
        }
        return deletedItem;
    }

    // endregion

    protected void nameActionButton(@NotNull Mode mode) {
        mode(mode);
        switch (mode) {
            case LIST, VIEW -> action.setText("Close");
            case EDIT, CREATE, DUPLICATE -> action.setText("Save");
            case DELETE -> action.setText("Delete");
        }
    }

    protected Tab addTabAt(@NotNull String tabText, @NotNull Component content, int position) {
        return tabSheet.add(new Tab(tabText), content, position);
    }

    protected <V> V fromBinder(String field) {
        return (V) binder().getBinding(field).orElseThrow().getField().getValue();
    }
}
