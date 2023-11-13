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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.value.ValueChangeMode;
import net.tangly.core.DateRange;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The entity view displays a list of entities in a grid.
 * <h2>CRUD Operations</h2>
 * <p>The view owns an optional form to display and edit details and an optional filter to filter desired entities in the
 * grid. Following operations are available through a pop-up menu:</p>
 * <dl>
 *     <dt>List</dt><dd>Display the grid with the entity properties. No form can be selected and no modification operations are available.</dd>
 *    <dt>View</dt><dd>Display a read-only form with the entity properties. If the form is displayed and another grid item is selected, the values of the selected entity are
 *    displayed in the form. </dd>
 *    <dt>Update</dt><dd>Display an editable form with the entity properties. If save operation is selected the updated values are stored into the entity. The grid is updated.</dd>
 *    <dt>Create</dt><dd>Display an editable empty form. If save operation is selected the entity is added to the underlying list and to the grid.</dd>
 *    <dt>Duplicate</dt><dd>Display an editable form with selected properties. If save operation is selected the entity is added to the underlying list and to the grid. The
 *    selected property values are specific to the entity type.</dd>
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
 * <h2>Form Declaration</h2>
 * <p>The form shall contains all visible or editable properties of an entity. The displayed entity is optional.
 * The create and duplicate mode open a form without a corresponding entity.</p>
 * <p>Beware that an entity could displays other items such as comments, tags, and relations to other entities.
 * These objects are often displayed as entity views in panels.</p>
 *
 * <h2>Menu Extensions</h2>
 * <p>Views can add menu options to perform an action on the selected item or on the whole list. A set of related actions are
 * added to the popup menu with a separation. Multiple blocks can be added.</p>
 * {@snippet :
 * var items = List.of(new AbstractMap.SimpleImmutableEntry(Mode.EDIT_TEXT,
 * (ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>>)((e)->form.edit(e.getItem().orElse(null)))));
 *}
 *
 * @param <T> Type of the displayed entities
 */
public abstract class ItemView<T> extends VerticalLayout {

    /**
     * Filter to select a subset of entities based on one or more property values of the desired items.
     *
     * @param <T> Type of the entity to filter
     */
    public abstract static class ItemFilter<T> {
        GridListDataView<T> dataView;

        protected ItemFilter() {
        }

        public static boolean matches(String value, String searchTerm) {
            boolean searchTermUndefined = (searchTerm == null) || (searchTerm.isBlank());
            return searchTermUndefined || ((value == null) || value.toLowerCase().contains(searchTerm.toLowerCase()));
        }

        protected void dataView(@NotNull GridListDataView<T> dataView) {
            if (this.dataView != null) {
                this.dataView.removeFilters();
            }
            this.dataView = dataView;
            dataView.addFilter(this::test);
        }

        protected GridListDataView<T> dataView() {
            return dataView;
        }

        public abstract boolean test(@NotNull T entity);

        protected void refresh() {
            dataView.refreshAll();
        }
    }

    private final Class<T> entityClass;
    private final BoundedDomain<?, ?, ?, ?> domain;
    private Provider<T> provider;

    private GridListDataView<T> dataView;
    private final ItemFilter<T> filter;
    private Mode mode;
    private GridContextMenu<T> menu;
    private final Grid<T> grid;

    private transient T entity;
    protected ItemForm<T, ?> form;
    private boolean isFormEmbedded;

    /**
     * Constructor of the class.
     *
     * @param entityClass class of the generic type
     * @param domain      optional domain to which the generic entity belongs to
     * @param provider    provider for instances of the entity to display in the grid
     * @param filter      optional filter for the grid
     * @param mode        mode of the view
     */
    protected ItemView(@NotNull Class<T> entityClass, BoundedDomain<?, ?, ?, ?> domain, @NotNull Provider<T> provider, ItemFilter<T> filter, @NotNull Mode mode) {
        this.entityClass = entityClass;
        this.domain = domain;
        this.filter = filter;
        isFormEmbedded(true);
        grid = new Grid<>();
        provider(provider);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("24em");
        add(grid);
        mode(mode);
    }

    protected abstract void init();

    public Class<T> entityClass() {
        return entityClass;
    }

    public BoundedDomain<?, ?, ?, ?> domain() {
        return domain;
    }

    public TypeRegistry registry() {
        return (domain != null) ? domain().registry() : null;
    }

    public Mode mode() {
        return mode;
    }

    public void mode(Mode mode) {
        this.mode = mode;
        if (Objects.nonNull(form)) {
            form.mode(mode);
        }
        buildMenu();
    }

    public T entity() {
        return entity;
    }

    public void entity(T entity) {
        this.entity = entity;
    }

    public boolean isFormEmbedded() {
        return isFormEmbedded;
    }

    public void isFormEmbedded(boolean isFormEmbedded) {
        this.isFormEmbedded = isFormEmbedded;
    }

    T selectedItem() {
        return grid().getSelectedItems().stream().findAny().orElse(null);
    }

    void selectedItem(T item) {
        grid().select(item);
    }

    Set<T> selectedItems() {
        return grid().getSelectedItems();
    }

    public static Component createTextFilterField(@NotNull Consumer<String> consumer) {
        var field = new TextField();
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.setClearButtonVisible(true);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.setWidthFull();
        field.getStyle().set("max-width", "100%");
        field.addValueChangeListener(e -> consumer.accept(e.getValue()));
        return field;
    }

    public static Component createIntegerFilterField(@NotNull Consumer<Integer> consumer) {
        var field = new IntegerField();
        field.setClearButtonVisible(true);
        field.addValueChangeListener(e -> consumer.accept(e.getValue()));
        return field;
    }

    public static Component createDateRangeField(@NotNull Consumer<DateRange> consumer) {
        var field = new DateRangePicker("interval", null, null);
        field.addValueChangeListener(e -> consumer.accept(e.getValue()));
        return field;
    }

    protected Provider<T> provider() {
        return provider;
    }

    protected GridContextMenu<T> menu() {
        if (Objects.isNull(menu)) {
            menu = grid().addContextMenu();
        }
        return menu;
    }

    /**
     * Set the provider. Vaadin generates a new data view when the items of the provider are added to the grid. The existing filter, if defined, is added to the new data view.
     *
     * @param provider new provider of the items displayed in the view
     */
    protected void provider(@NotNull Provider<T> provider) {
        this.provider = provider;
        dataView = grid.setItems(DataProvider.ofCollection(provider.items()));
        if (filter() != null) {
            filter().dataView(dataView);
        }
        dataView.refreshAll();
    }

    protected ItemFilter<T> filter() {
        return filter;
    }

    protected Grid<T> grid() {
        return grid;
    }

    protected GridListDataView<T> dataView() {
        return dataView;
    }

    protected void buildMenu() {
        if (mode.hasForm()) {
            menu().removeAll();
            menu().addItem(Mode.VIEW_TEXT, event -> event.getItem().ifPresent(form::display));
            if (!mode().readonly()) {
                menu().add(new Hr());
                menu().addItem(Mode.EDIT_TEXT, event -> event.getItem().ifPresent(form::edit));
                menu().addItem(Mode.CREATE_TEXT, event -> form.create());
                menu().addItem(Mode.DUPLICATE_TEXT, event -> event.getItem().ifPresent(form::duplicate));
                menu().addItem(Mode.DELETE_TEXT, event -> event.getItem().ifPresent(form::delete));
            }
            SingleSelect<Grid<T>, T> selection = grid.asSingleSelect();
            selection.addValueChangeListener(e -> form.value(e.getValue()));
        }
    }

    protected HeaderRow createHeaderRow() {
        grid().getHeaderRows().clear();
        return grid().appendHeaderRow();
    }

    protected void addFilterText(@NotNull HeaderRow headerRow, @NotNull String key, @NotNull Consumer<String> attribute) {
        headerRow.getCell(grid().getColumnByKey(key)).setComponent(createTextFilterField(attribute));
    }
}
