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
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.DateRange;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.Operation;
import net.tangly.core.providers.Provider;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.View;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The entity view displays a list of entities in a grid. The view has three main scenarios. First, it is used as a read-only list of entities.
 * Second, it is used as a read-only view with a menu item to display the details of an entity.
 * Third, it is used as an editable view with a form to view, create, duplicate, edit and delete the entity.
 * These operations are provided through a context menu. If the view is set to read-only, only the CRUD view operation is available.
 * <p>Specific situations are supported through helper functions. You can define a custom menu for your scenario.</p>
 * <h2>CRUD Operations</h2>
 * <p>The view owns an optional form to display and edit details and an optional filter to filter desired entities in the
 * grid. Following operations are available through a pop-up menu:</p>
 * <dl>
 *     <dt>List</dt><dd>Display the grid with the entity properties. No form can be selected and no modification operations are available.
 *     No context sensitive menu is displayed. The view is implicitly read-only.</dd>
 *    <dt>View</dt><dd>Display a read-only form with the entity properties.</dd>
 *    <dt>Update</dt><dd>Display an editable form with the entity properties. If the save operation is selected the updated values are stored into the entity.
 *    The grid is updated.</dd>
 *    <dt>Create</dt><dd>Display an editable empty form. If the save operation is selected the entity is added to the underlying list and to the grid.</dd>
 *    <dt>Duplicate</dt><dd>Display an editable form with selected properties. If the save operation is selected the entity is added to the underlying list and
 *    to the grid. The selected property values are specific to the entity type.</dd>
 *    <dt>Delete</dt><dd>Display a read-only form with the entity properties. If the delete operation is selected the entity is removed from the underlying list
 *    and from the grid.</dd>
 * </dl>
 * <h2>Form Functions</h2>
 * <p>You must register a from object to have access to the form functions. The operations on the entity through the form are:</p>
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
 * <h2>Menu Extensions</h2>
 * <p>Views can add menu options to perform an action on the selected item or on the whole list. A set of related actions are
 * added to the popup menu with a separation. Multiple blocks can be added.</p>
 * <p>Separate menu actions specific to selected items from global menu actions. The first block should be the view related CRUD operations.
 * A second optional block should contain actions for selected items.
 * A third optional blocks should contains global actions not related to selected items.</p>
 * {@snippet :
 * var items = List.of(new AbstractMap.SimpleImmutableEntry(Mode.EDIT_TEXT,
 * (ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>>)((e)->form.edit(e.getItem().orElse(null)))));
 *}
 * <h2>Filtering</h2>
 * <p>You can add filtering capablitities to the view. The user can input filter criteria used to select which entities are displayed in the grid.</p>
 * <h2>Access Rights</h2>
 * <p>Access rights determines which menu items are available. The derived class can use the rights to limit data availability.
 * Access rights are used to create the adequate provider. This operation can be outside the view or in the derived view.
 * The class constructor has a provider parameter to support the first approach.</p>
 *
 * @param <T> Type of the displayed entities
 */
public abstract class ItemView<T> extends VerticalLayout implements View {
    public static final String OID = "oid";
    public static final String OID_LABEL = "OID";

    public static final String ID = "id";
    public static final String ID_LABEL = "ID";

    public static final String NAME = "name";
    public static final String NAME_LABEL = "Name";

    public static final String TEXT = "text";
    public static final String TEXT_LABEL = "Text";

    public static final String FROM = "from";
    public static final String FROM_LABEL = "From";

    public static final String TO = "to";
    public static final String TO_LABEL = "To";

    public static final String DOMAIN = "domain";
    public static final String DOMAIN_LABEL = "DOMAIN";

    public static final String DATE = "date";
    public static final String DATE_LABEL = "Date";

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

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
    private final BoundedDomainUi<?> domain;
    private Provider<T> provider;

    private GridListDataView<T> dataView;
    private final ItemFilter<T> filter;
    private final Mode mode;
    private boolean readonly;
    private GridContextMenu<T> menu;
    private final Grid<T> grid;

    private transient T entity;
    private LazyReference<ItemForm<T, ?>> form;
    private final boolean isViewEmbedded;
    private boolean isFormEmbedded;

    /**
     * Constructor of the class. The access rights are used to filter the data the user can see and modify.
     * The filtering is often realized by defining a filtering provider other the given data provider.
     * <p>A view has always access rights. How the view interprets them is up the specific implementation.</p>
     * <p>The menu is refreshed each time the access rights or the mode is changed. The mode attribute supersedes the access rights when determining the
     * content of the menu.</p>
     *
     * @param entityClass class of the generic type.
     * @param domain      user interface domain to which the view belongs to
     * @param provider    provider for instances of the entity to display in the grid
     * @param filter      optional filter for the grid
     * @param mode        mode of the view: LIST, VIEW, or EDITABLE
     */
    protected ItemView(@NotNull Class<T> entityClass, BoundedDomainUi<?> domain, @NotNull Provider<T> provider, ItemFilter<T> filter, @NotNull Mode mode,
                       boolean isViewEmbedded) {
        this.entityClass = entityClass;
        this.domain = domain;
        this.filter = filter;
        this.mode = mode;
        this.isViewEmbedded = isViewEmbedded;
        isFormEmbedded(true);
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("24em");
        add(grid);
        provider(provider);
        readonly(mode.readonly());
    }

    protected ItemView(@NotNull Class<T> entityClass, BoundedDomainUi<?> domain, @NotNull Provider<T> provider, ItemFilter<T> filter, @NotNull Mode mode) {
        this(entityClass, domain, provider, filter, mode, false);
    }

    public Class<T> entityClass() {
        return entityClass;
    }

    public BoundedDomainUi<?> domainUi() {
        return domain;
    }

    /**
     * Delegate method returning the domain of the view.
     *
     * @return domain of the view
     */
    public BoundedDomain<?, ?, ?> domain() {
        return domain.domain();
    }

    /**
     * Delegate method returning the type registry of the domain.
     * @return type registry of the domain
     */
    public TypeRegistry registry() {
        return (domain() != null) ? domain().registry() : null;
    }

    public final Mode mode() {
        return mode;
    }

    public final boolean readonly() {
        return readonly;
    }

    public final void readonly(boolean readonly) {
        this.readonly = readonly;
        buildMenu();
    }

    public T entity() {
        return entity;
    }

    public void entity(T entity) {
        this.entity = entity;
    }

    /**
     * Returns true if the form is embedded in the view.
     *
     * @return true if the form is embedded in the view
     * @see #isFormEmbedded(boolean)
     */
    public boolean isFormEmbedded() {
        return isFormEmbedded;
    }

    /**
     * Sets the form embedded attribute. If the form is embedded in the view, the form is displayed in the view. If the form is not embedded, the form is
     * displayed.
     *
     * @param isFormEmbedded new value of the property
     * @see #isFormEmbedded()
     */
    public final void isFormEmbedded(boolean isFormEmbedded) {
        this.isFormEmbedded = isFormEmbedded;
    }

    public final boolean isViewEmbedded() {
        return isViewEmbedded;
    }

    protected final LazyReference<ItemForm<T, ?>> form() {
        return form;
    }

    protected final void form(@NotNull Supplier<ItemForm<T, ?>> supplier) {
        form = new LazyReference<>(supplier);
        if (Objects.nonNull(form)) {
            SingleSelect<Grid<T>, T> selection = grid.asSingleSelect();
            selection.addValueChangeListener(e -> form.get().value(e.getValue()));
        }

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

    public void refresh() {
        if (Objects.nonNull(dataView)) {
            dataView.refreshAll();
        }
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
        var field = new DateRangePicker(null, "Select Date Range", null, null);
        field.addValueChangeListener(e -> consumer.accept(e.getValue()));
        return field;
    }

    public Provider<T> provider() {
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
    public void provider(@NotNull Provider<T> provider) {
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

    protected final Grid<T> grid() {
        return grid;
    }

    protected final GridListDataView<T> dataView() {
        return dataView;
    }

    protected void buildMenu() {
        if (mode() != Mode.LIST) {
            menu().removeAll();
            buildCrudMenu(mode);
        }
        addActions(menu());
    }

    protected void buildCrudMenu(Mode mode) {
        if (mode != Mode.LIST) {
            menu().addItem(Mode.VIEW_TEXT, event -> event.getItem().ifPresent(o -> form().get().display(o)));
        }
        if (!mode.readonly()) {
            menu().add(new Hr());
            menu().addItem(Operation.EDIT_TEXT, event -> event.getItem().ifPresent(o -> form().get().edit(o)));
            menu().addItem(Operation.CREATE_TEXT, _ -> form.get().create());
            menu().addItem(Operation.DUPLICATE_TEXT, event -> event.getItem().ifPresent(o -> form().get().duplicate(o)));
            menu().addItem(Operation.DELETE_TEXT, event -> event.getItem().ifPresent(o -> form().get().delete(o)));
        }
    }

    /**
     * Add custom actions to the context menu. Overwrite the menu if you want to add actions to the context menu.
     * The mode of the view is available through the {@link #mode()}}. The read-only attribute is available through the {@link #readonly()} method.
     * The menu can also be retrieved through the {@link #menu()} method.
     *
     * @param menu context menu of the grid
     */
    protected void addActions(@NotNull GridContextMenu<T> menu) {
    }

    protected HeaderRow createHeaderRow() {
        grid().getHeaderRows().clear();
        return grid().appendHeaderRow();
    }

    protected void addFilterText(@NotNull HeaderRow headerRow, @NotNull String key, @NotNull Consumer<String> attribute) {
        headerRow.getCell(grid().getColumnByKey(key)).setComponent(createTextFilterField(attribute));
    }
}
