/*
 * Copyright 2006-2023 Marcel Baumann
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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import net.tangly.core.Entity;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Defines a view for a list of referenced entities displayed in a grid. The details of the selected referenced entity can be shown. The following actions are supported:
 * <ul>
 *     <li>View the list of one2many entities. The columns of the grid can be tailored through the gridConfigurator parameter.
 *     The default configuration displays the object identifier, external identifier, name, from date, and to date.</li>
 *     <li>View the details of the selected entity and close the view. </li>
 *     <li>Delete the selected reference. The operation is not reversible.</li>
 *     <li>Add a new reference to the list. A selection grid is opened to select the entity to add.</li>
 * </ul>
 * <p> All the above operations are delegated to the view responsible to display the items of the one2many relation.</p>
 * <h2>Implementation</h2>
 * <p>The first view displays the existing relations between the owning entities and the owned objects.</p>
 * <p>The second view displays all the instances of the owned type.
 * Two scenarios exist. First, objects can be added to the selected ones. Second, objects can be removed from the selected ones.
 * Upon confirmation, the selected objects are either added or removed.</p>
 * <p>The logic is:</p>
 * <dl>
 *     <dt>Items to remvoe</dt><dd>itemsToRemove.addAll(originalList).removeAll(newList)</dd>
 *     <dt>Items to add</dt><dd>itemsToAdd.addAll(newList).removeAll(originalList)</dd>
 * </dl>
 *
 * @param <O> type of the owning entity
 * @param <T> type of the entities referenced in the one-to-many relations
 */
public class One2ManyView<O extends Entity, T extends Entity> extends VerticalLayout {
    private final Provider<T> provider;
    private final One2OneField.RelationView<T> view;
    private final Button update;

    public One2ManyView(@NotNull String relation, @NotNull Provider<T> provider, Function<O, List<T>> items, Function<O, T> add, Function<O, T> remove) {
        this(relation, provider);
    }

    public One2ManyView(@NotNull String relation, @NotNull Provider<T> provider) {
        this.provider = provider;
        this.view = new One2OneField.RelationView<>(provider, Grid.SelectionMode.NONE);
        update = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
        update.addClickListener(e -> displayRelationships());
        add(view, new HorizontalLayout(update));
    }

    public void setReadOnly(boolean readOnly) {
        update.setEnabled(!readOnly);
    }

    private void displayRelationships() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setResizable(true);
        One2OneField.RelationView<T> view = new One2OneField.RelationView<>(provider, Grid.SelectionMode.MULTI);
        dialog.add(new VerticalLayout(view, new HtmlComponent("br"), createFormButtons(dialog, view)));
        dialog.open();
    }

    private HorizontalLayout createFormButtons(@NotNull Dialog dialog, @NotNull One2OneField.RelationView<T> view) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        Button cancel = new Button("Cancel", event -> dialog.close());
        Button clear = new Button("Remove", event -> {
            view.selectedItems().forEach(provider::delete);
            dialog.close();
        });
        Button select = new Button("Add", event -> {
            view.selectedItems().forEach(provider::update);
            dialog.close();
        });
        actions.add(cancel, clear, select);
        return actions;
    }
}
