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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import net.tangly.core.MutableEntity;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The view displays all items of a property backed by a collection. Two operations are supported. You can remove selected items from the collection. You can select items from a
 * list and add them to the collection.
 * <p>The List mode is used when read-only state is applied to the owning entity. No menu items are available.
 * The view mode is used when the entity is editable. The menu items povides add and remove of items from the collection. If the view of the collection field has a form, a view
 * menu item is provided.
 * </p>
 * <p>The add action opens a dialog with a list of all entities, which could be added to the relation.
 * The user can select multiple items and either add them to the displayed relation or discard the changes. The changes are only performed in the user interface and will only be
 * propagated to the model if the save action is triggered.</p>
 *
 * @param <T> type of the entities in the list.
 */
@Tag("tangly-field-one2many-references")
public class One2ManyReferencesField<T extends MutableEntity> extends CustomField<Collection<T>> {
    private final Class<T> entityClass;
    private final transient Provider<T> selectables;
    private final EntityView<T> view;

    public One2ManyReferencesField(@NotNull Class<T> entityClass, @NotNull Provider<T> selectables) {
        this.entityClass = entityClass;
        this.selectables = selectables;
        this.view = EntityView.ofLIST(entityClass, null, ProviderInMemory.of());
        setHeightFull();
        setWidthFull();
        addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        view.addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        add(view);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        view.menu().removeAll();
        if (!readOnly)
            view.menu().addItem("Add", event -> displayAddDialog());
        view.menu().addItem("Remove", event -> {
            view.provider().delete(view.selectedItem());
            view.dataView().refreshAll();
        });
    }

    @Override
    public void clear() {
        super.clear();
        view.provider(ProviderInMemory.of());
    }

    @Override
    public Collection<T> generateModelValue() {
        return view.provider().items();
    }

    @Override
    protected void setPresentationValue(Collection<T> items) {
        view.provider(Objects.isNull(items) ? ProviderInMemory.of() : ProviderInMemory.of(List.copyOf(items)));
        setValue(view.provider().items());
    }

    private void displayAddDialog() {
        Dialog dialog = VaadinUtils.createDialog();
        EntityView<T> selectionView = EntityView.ofLIST(entityClass, null, selectables);
        dialog.add(new VerticalLayout(selectionView, new HtmlComponent("br"), createFormButtons(dialog, view, selectionView)));
        dialog.open();
    }

    private HorizontalLayout createFormButtons(@NotNull Dialog dialog, @NotNull EntityView<T> view, @NotNull EntityView<T> selectionView) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        Button cancel = new Button("Cancel", event -> dialog.close());
        Button change = new Button("Update", event -> {
            view.provider().update(selectionView.selectedItem());
            view.dataView().refreshAll();
            dialog.close();
        });
        actions.add(cancel, change);
        return actions;
    }
}
