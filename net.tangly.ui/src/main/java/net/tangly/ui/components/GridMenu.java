/*
 * Copyright 2024 Marcel Baumann
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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Hr;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.domain.Operation;
import net.tangly.ui.app.domain.Cmd;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a grid menu abstraction to support a dynamic context menu in item views. Menu items can be enabled or disabled based on the fact if items are
 * seleected or not.
 *
 * @param <T> type of the items in the grid
 */
public class GridMenu<T> {
    /**
     * Defines the type of a menu item. The type of the menu item determines if the item is enabled or disabled based on the fact if items are selected
     * <dl>
     *     <dt>ITEM</dt><dd>the item is enabled if at least one item is selected</dd>
     *     <dt>GLOBAL</dt><dd>the item is enabled if no item is selected</dd>
     *     <dt>DUAL</dt><dd>the item is always enabled</dd>
     * </dl>
     */
    public enum MenuItemType {
        ITEM, GLOBAL, DUAL;

        public boolean isEnabled(boolean itemSelected) {
            return switch (this) {
                case ITEM -> itemSelected;
                case GLOBAL -> !itemSelected;
                case DUAL -> true;
            };
        }
    }

    private GridContextMenu<T> menu;
    private Map<GridMenuItem<T>, MenuItemType> menuItems;

    public GridMenu(@NotNull Grid<T> grid) {
        this.menu = grid.addContextMenu();
        menu.setDynamicContentHandler(_ -> {
            update(!grid.getSelectedItems().isEmpty());
            return !menuItems.isEmpty();
        });
        this.menuItems = new HashMap<>();
    }

    public GridContextMenu<T> menu() {
        return menu;
    }

    public void clear() {
        menuItems.clear();
        menu.removeAll();
    }

    public void update(boolean itemSelected) {
        menuItems.entrySet().forEach(entry -> entry.getKey().setEnabled(entry.getValue().isEnabled(itemSelected)));
    }

    public void add(@NotNull String label, @NotNull Cmd cmd, @NotNull MenuItemType type) {
        add(label, e -> cmd.execute(), type);
    }

    public void add(@NotNull String label, @NotNull ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>> clickListener,
                    @NotNull MenuItemType type) {
        var menuItem = menu.addItem(label, clickListener);
        menuItems.put(menuItem, type);
    }

    public void add(@NotNull Component component) {
        menu.add(component);
    }

    /**
     * Creates the standard CRUD menu for an item view. The menu is built based on the mode of the grid.
     *
     * @param mode mode of the grid
     * @param form optional form to display the item. If the form has the <code>LIST</code> mode, no form is displayed.
     */
    public void buildCrudMenu(@NotNull Mode mode, LazyReference<ItemForm<T, ?>> form) {
        if (mode != Mode.LIST) {
            add(Mode.VIEW_TEXT, e -> e.getItem().ifPresent(o -> form.get().display(o)), MenuItemType.ITEM);
        }
        if (!mode.readonly()) {
            menu().add(new Hr());
            add(Operation.CREATE_TEXT, _ -> form.get().create(), MenuItemType.GLOBAL);
            add(Operation.EDIT_TEXT, event -> event.getItem().ifPresent(o -> form.get().edit(o)), MenuItemType.ITEM);
            add(Operation.DUPLICATE_TEXT, event -> event.getItem().ifPresent(o -> form.get().duplicate(o)), MenuItemType.ITEM);
            add(Operation.DELETE_TEXT, event -> event.getItem().ifPresent(o -> form.get().delete(o)), MenuItemType.ITEM);
        }
    }
}
