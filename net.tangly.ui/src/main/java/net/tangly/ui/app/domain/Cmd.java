/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * The interface defines the abstraction for the command pattern. A command is an action mostly executed through the user interface as a client. A command is an object whose role
 * is to store all the information required for executing an action, including the method to call, the method arguments, and the object (known as the receiver) that implements
 * the method.
 */
@FunctionalInterface
public interface Cmd {
    /**
     * Factory method to create a command from a grid context menu item click event. The command is executed on the item selected in the grid.
     * An item must be selected to trigger the command.
     *
     * @param event    grid context menu item click event
     * @param consumer consumer to execute the command
     * @param <T>      type of the item
     */
    static <T> void ofItemCmd(@NotNull GridContextMenu.GridContextMenuItemClickEvent<T> event, @NotNull Consumer<T> consumer) {
        event.getItem().ifPresent(consumer);
    }

    /**
     * Factory method to create a command from a grid context menu item click event. The command is executed if no item is selected in the grid.
     *
     * @param event    grid context menu item click event
     * @param consumer consumer to execute the command
     */
    static void ofGlobalCmd(@NotNull GridContextMenu.GridContextMenuItemClickEvent<?> event, @NotNull Runnable consumer) {
        if (event.getItem().isEmpty()) {
            consumer.run();
        }
    }

    /**
     * Factory method to create a command from a grid context menu item click event. The command is executed if one or no items are selected in the grid.
     * The consumer receives the selected item in the grid or null if no item is selected.
     *
     * @param event    grid context menu item click event
     * @param consumer consumer to execute the command
     */
    static <T> void ofDualCmd(@NotNull GridContextMenu.GridContextMenuItemClickEvent<T> event, @NotNull Consumer<T> consumer) {
        var selectectItems = event.getGrid().getSelectedItems();
        consumer.accept(selectectItems.isEmpty() ? null : selectectItems.stream().findAny().orElse(null));
    }

    /**
     * Executes the command.
     */
    void execute();

    /**
     * Factory method to create a dialog with a form layout. The dialog is resizable and has a fixed width.
     *
     * @param width width of the dialog
     * @param form  form layout to display in the dialog
     * @return dialog with the form layout
     */
    static Dialog createDialog(@NotNull String width, @NotNull FormLayout form) {
        Dialog dialog = new Dialog();
        dialog.setWidth(width);
        dialog.setResizable(true);
        dialog.add(form);
        return dialog;
    }

    /**
     * Returns true if the command is enabled. The criteria are if an item is selected in the grid and the access rights associated with the user profile.
     *
     * @param itemSelected true if an item is selected in the grid
     * @return true if the command is enabled, otherwise false
     */
    default boolean isEnabled(boolean itemSelected) {
        return true;
    }

    /**
     * Returns the dialog associated with the command if existing.
     *
     * @return associated dialog if defined othwrwise null
     */
    default Dialog dialog() {
        return null;
    }
}
