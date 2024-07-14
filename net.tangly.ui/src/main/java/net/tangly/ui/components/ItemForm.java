/*
 * Copyright 2023-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.core.HasMutableText;
import net.tangly.core.MutableEntity;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.Operation;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * Defines the CRUD contract for a form used to display or modify an entity. The abstract methods are:
 * <dl>
 *    <dt>{@link #value(Object)}</dt><dd>Fill the form with properties of the entity and business logic.</dd>
 *    <dt>{@link #clear()}</dt><dd>Clear all fields in the form</dd>
 *    <dt>{@link #createOrUpdateInstance(Object)}</dt><dd>Create a new entity based on the fields and business logic.</dd>
 * </dl>
 * <p>A form has a binder to transfer values between the form fields and the entity. The form has also a reference to the view containing the entities, which could be displayed
 * in the form.</p>
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
    private Operation operation;
    private Dialog dialog;

    protected ItemForm(@NotNull U parent) {
        this.parent = parent;
        binder = new Binder<>(parent.entityClass());
        operation = Operation.NONE;
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
            switch (operation) {
                case EDIT, CREATE, DUPLICATE, REPLACE -> updateEntity();
                case DELETE -> deleteEntity();
            }
            closeForm();
        });
        form().add(tabSheet, createButtonBar());
    }

    static <T extends MutableEntity> ProviderView<T> ofUnselected(@NotNull Provider<T> provider, @NotNull Collection<T> selected) {
        return new ProviderView<>(provider, o -> !selected.contains(o));
    }

    /**
     * Creates a code combobox field with the specified code type and label.
     * The combobox is configured to display the code of the code type.
     * @param codeType type of the table code. It contains all code values
     * @param label label of the combobox
     * @return a combobox field with the specified code type and label
     * @param <T> type of the code
     */
    public static <T extends Code> ComboBox<T> createCodeField(@NotNull CodeType<T> codeType, @NotNull String label) {
        ComboBox<T> codeField = new ComboBox<>(label);
        codeField.setItemLabelGenerator(o -> (Objects.isNull(o) ? "" : o.code()));
        codeField.setItems(codeType.codes());
        codeField.setPlaceholder("select item");
        codeField.setOpened(false);
        return codeField;
    }

    /**
     * Creates a form with an AsciiDoc field to edit the text of the entity.
     * @param text
     * @return
     */
    public static FormLayout textForm(@NotNull AsciiDocField text) {
        var form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        form.add(text, 3);
        return form;
    }

    public FormLayout textForm() {
        var text = new AsciiDocField("Text");
        ((Binder<HasMutableText>) binder()).bind(text, HasMutableText::text, HasMutableText::text);
        return textForm(text);
    }

    /**
     * Returns the operation of the form.
     *
     * @return the operation of the form
     * @see #operation(Operation)
     */
    public Operation operation() {
        return operation;
    }

    /**
     * Sets the operation of the form and propagates the readonly status to the form fields.
     * Overwrite the method to update form components accordingly.
     *
     * @param operation operation of the form
     * @see #operation()
     */
    public void operation(@NotNull Operation operation) {
        this.operation = operation;
        binder.getFields().forEach(o -> o.setReadOnly(operation.readonly()));
    }

    /**
     * Returns the value displayed in the form.
     *
     * @return value displayed in the form or null.
     */
    public T value() {
        return value;
    }

    /**
     * Sets the value displayed in the form. If the value is null, the form fields are reset to empty values.
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
        display(entity, Operation.VIEW);
    }

    /**
     * Display an entity with properties for modification.
     *
     * @param entity entity to modify
     */
    public void edit(@NotNull T entity) {
        display(entity, Operation.EDIT);
    }

    /**
     * Create a new entity form.
     */
    public void create() {
        display(null, Operation.CREATE);
    }

    /**
     * Display and support editing of a duplicated entity.
     *
     * @param entity entity to duplicate
     */
    public void duplicate(@NotNull T entity) {
        display(entity, Operation.DUPLICATE);
        this.value = null;
    }

    /**
     * Display an entity with properties for deletion.
     *
     * @param entity entity to delete.
     */
    public void delete(@NotNull T entity) {
        display(entity, Operation.DELETE);
    }

    // endregion

    // region Form functions

    /**
     * Display the value in the form. The form is revealed in the user interface. The new mode is used to propagate the mode and configure the action buttons.
     *
     * @param value     value to display
     * @param operation operation to perform
     */
    protected void display(T value, @NotNull Operation operation) {
        operation(operation);
        action.setText(operation.confirmationText());
        value(value);
        if (parent.isFormEmbedded()) {
            parent.add(formLayout);
        } else {
            dialog = VaadinUtils.createDialog();
            dialog.add(formLayout);
            dialog.open();
        }
    }

    protected void closeForm() {
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
     * @return domain containing the CRUD form command button
     */
    protected Component createButtonBar() {
        return new HorizontalLayout(cancel, action);
    }

    // endregion

    // region Entity related functions

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

    protected Tab addTabAt(@NotNull String tabText, @NotNull Component content, int position) {
        return tabSheet.add(new Tab(tabText), content, position);
    }

    protected <V> V fromBinder(String field) {
        return (V) binder().getBinding(field).orElseThrow().getField().getValue();
    }
}
