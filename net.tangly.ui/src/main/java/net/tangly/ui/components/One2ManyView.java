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

import java.util.Objects;
import java.util.function.Consumer;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.core.QualifiedEntity;
import net.tangly.core.providers.Provider;
import net.tangly.ui.grids.PaginatedGrid;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a view for a list of referenced entities displayed in a grid. The details of the selected referenced entity can be shown. The following actions are
 * supported:
 * <ul>
 *     <li>View the list of one2many entities. The columns of the grid can be tailored through the gridConfigurator parameter. The default configuration
 *     displays the object identifier, external identifier and name.</li>
 *     <li>View the details of the selected entity and close the view. </li>
 *     <li>View the details of the selected entity and remove it for the list of referenced entities. The view is readonly.</li>
 *     <li>Update the details of the selected entity.</li>
 *     <li>Delete the selected entity. The operation is not reversible.</li>
 *     <li>Add a new reference to the list. A selection grid is opened to select the entity to add.</li>
 * </ul>
 * <p> All the above operations are delegated to the view responsible to display the items of the one2many relation.</p>
 *
 * @param <T> type of the entities referenced in the one 2 many relationship
 */
public class One2ManyView<T> extends VerticalLayout {
    private final Crud.Mode mode;
    private final EntitiesView<T> view;
    private final Provider<T> provider;
    private final PaginatedGrid<T> grid;
    private T selectedItem;

    private Button details;
    private Button update;
    private Button insert;
    private Button remove;

    public One2ManyView(@NotNull Class<T> entityClass, @NotNull Crud.Mode mode, @NotNull Consumer<Grid<T>> gridConfigurator, @NotNull Provider<T> provider,
                        EntitiesView<T> view) {
        this.mode = mode;
        this.view = view;
        this.provider = provider;
        this.grid = new PaginatedGrid<>();
        grid.dataProvider(DataProvider.ofCollection(provider.items()));
        grid.asSingleSelect().addValueChangeListener(event -> selectItem(event.getValue()));
        gridConfigurator.accept(grid);
        setSizeFull();
        add(grid, createCrudButtons());
        selectItem(null);
    }

    public static <E extends QualifiedEntity> void defineGrid(@NotNull Grid<E> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(QualifiedEntity::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    public HorizontalLayout createCrudButtons() {
        details = new Button("Details", VaadinIcon.ELLIPSIS_H.create(), event -> displayDialog(CrudForm.Operation.VIEW));
        update = new Button("Update", VaadinIcon.PENCIL.create(), event -> displayDialog(CrudForm.Operation.UPDATE));
        insert = new Button("Add", VaadinIcon.PLUS.create(), event -> displayDialog(CrudForm.Operation.CREATE));
        remove = new Button("Delete", VaadinIcon.TRASH.create(), event -> displayDialog(CrudForm.Operation.DELETE));

        update.setEnabled(mode.canUpdate());
        insert.setEnabled(mode.canAdd());
        remove.setEnabled(mode.canDelete());

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(insert, remove, update, details);
        return actions;
    }

    private void displayDialog(CrudForm.Operation operation) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setResizable(true);
        FormLayout form = view.createForm(operation, operation != CrudForm.Operation.CREATE ? selectedItem : null);
        CrudActionsListener<T> actionsListener = new GridActionsListener<>(provider, grid.getDataProvider(), this::selectItem);
        dialog.add(
            new VerticalLayout(form, new HtmlComponent("br"), view.createFormButtons(dialog, operation, mode.isCancellable(), selectedItem, actionsListener)));
        dialog.open();
    }

    private void selectItem(T item) {
        selectedItem = item;
        if (Objects.nonNull(item)) {
            details.setEnabled(true);
            update.setEnabled(mode.canUpdate());
            remove.setEnabled(mode.canDelete());
        } else {
            details.setEnabled(false);
            update.setEnabled(false);
            remove.setEnabled(false);
        }
        insert.setEnabled(mode.canAdd());
    }
}
