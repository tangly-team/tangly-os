/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.components;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.core.Entity;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

import static net.tangly.ui.components.ItemView.createTextFilterField;

/**
 * A composite field to display a one two one relationship instance. The unique name of the referenced instance is shown. The button allows the change of the relationship
 * instance.
 * <p>The referenced object is stored as field. The displayed fields are updated through {@link One2OneField#setPresentationValue(Entity)} ()}.</p>
 *
 * @param <T> type of the entity referenced in the one to one relationship
 */
@Tag("tangly-field-one2one")
public class One2OneField<T extends Entity> extends CustomField<T> {
    private final Provider<T> provider;
    private final TextField oid;
    private final TextField id;
    private final TextField name;
    private final Button update;

    static class RelationView<T extends Entity> extends VerticalLayout {
        private final Provider<T> provider;
        private final Grid<Entity> grid;
        private final EntityFilter<T> filter;

        RelationView(@NotNull Provider<T> provider, Grid.SelectionMode selectionMode) {
            this.provider = provider;
            grid = new Grid<>();
            grid.setSelectionMode(selectionMode);
            grid.addThemeVariants(GridVariant.LUMO_COMPACT);

            filter = new EntityFilter<>();
            init();
        }

        T selectedItem() {
            return (T) grid.getSelectedItems().stream().findAny().get();
        }

        void selectedItem(T item) {
            grid.select(item);
        }

        Set<T> selectedItems() {
            return (Set<T>) grid.getSelectedItems();
        }

        private void init() {
            grid.addColumn(Entity::oid).setKey(EntityView.OID).setHeader(EntityView.OID_LABEL).setSortable(true).setFlexGrow(0).setWidth("10em");
            grid.addColumn(Entity::id).setKey(EntityView.ID).setHeader(EntityView.ID_LABEL).setSortable(true).setFlexGrow(0).setWidth("10em");
            grid.addColumn(Entity::name).setKey(EntityView.NAME).setHeader(EntityView.NAME_LABEL).setSortable(false).setFlexGrow(0).setWidth("20em");
            grid.addColumn(Entity::from).setKey(EntityView.FROM).setHeader(EntityView.FROM_LABEL).setSortable(false).setFlexGrow(0).setWidth("20em");
            grid.addColumn(Entity::to).setKey(EntityView.FROM_LABEL).setHeader(EntityView.TO_LABEL).setSortable(false).setFlexGrow(0).setWidth("20em");

            grid.getHeaderRows().clear();
            HeaderRow headerRow = grid.appendHeaderRow();
            headerRow.getCell(grid.getColumnByKey(EntityView.ID)).setComponent(createTextFilterField(filter::id));
            headerRow.getCell(grid.getColumnByKey(EntityView.NAME)).setComponent(createTextFilterField(filter::name));

            grid.setDataProvider((DataProvider<Entity, ?>) DataProvider.ofCollection(provider.items()));

            add(grid);
        }
    }

    public One2OneField(@NotNull String relation, @NotNull Provider<T> provider) {
        this.provider = provider;
        setLabel(relation);
        oid = VaadinUtils.createTextField(EntityView.OID_LABEL, EntityView.OID, true, false);
        id = VaadinUtils.createTextField(EntityView.ID_LABEL, EntityView.ID, true, false);
        name = VaadinUtils.createTextField(EntityView.NAME_LABEL, EntityView.NAME, true, false);
        update = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
        update.addClickListener(e -> displayRelationships());
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.add(oid, id, name, update);
        add(layout);
    }


    @Override
    protected T generateModelValue() {
        return super.getValue();
    }

    @Override
    protected void setPresentationValue(T one2one) {
        if (Objects.isNull(one2one)) {
            clear();
        } else {
            oid.setValue(Long.toString(one2one.oid()));
            id.setValue(one2one.id());
            name.setValue(one2one.name());
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        update.setEnabled(!isReadOnly());
    }

    @Override
    public void clear() {
        super.clear();
        oid.clear();
        id.clear();
        name.clear();
    }

    private void displayRelationships() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setResizable(true);
        RelationView<T> view = new RelationView<>(provider, Grid.SelectionMode.SINGLE);
        dialog.add(new VerticalLayout(view, new HtmlComponent("br"), createFormButtons(dialog, view)));
        view.selectedItem(getValue());
        dialog.open();
    }

    private HorizontalLayout createFormButtons(@NotNull Dialog dialog, @NotNull RelationView<T> view) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        Button cancel = new Button("Cancel", event -> dialog.close());
        Button clear = new Button("Remove", event -> {
            clear();
            dialog.close();
        });
        Button select = new Button("Update", event -> {
            setValue(view.selectedItem());
            dialog.close();
        });
        actions.add(cancel, clear, select);
        return actions;
    }
}
