/*
 * Copyright 2023 Marcel Baumann
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

import org.jetbrains.annotations.NotNull;

/**
 * Define the modes of the item view.
 * <dl>
 *     <dt>LIST</dt><dd>The list mode displays a read-only grid of entities without any menu items.</dd>
 *     <dt>VIEW</dt><dd>The view mode displays a read-only grid of entities with a details form.</dd>
 *     <dt>EDIT</dt><dd>The edit mode displays a grid of entities with a details form for edition and creation. The edit mode supports edition, creation, and duplication.</dd>
 *     <dt>DELETE</dt><dd>Teh delete mode displays a grid of entities with a read-only details form for deletion.</dd>
 * </dl>
 */
public enum Mode {
    LIST(Mode.LIST_TEXT), VIEW(Mode.VIEW_TEXT), EDIT(Mode.EDIT_TEXT), CREATE(Mode.CREATE_TEXT), DUPLICATE(Mode.DUPLICATE_TEXT), DELETE(Mode.DELETE_TEXT);

    public static final String LIST_TEXT = "List";
    public static final String VIEW_TEXT = "View";
    public static final String EDIT_TEXT = "Edit";
    public static final String CREATE_TEXT = "Create";
    public static final String DUPLICATE_TEXT = "Duplicate";
    public static final String DELETE_TEXT = "Delete";

    private final String text;

    Mode(@NotNull String text) {
        this.text = text;
    }

    /**
     * Propagated mode for subcomponents of a view or a form.
     *
     * @param mode mode of the parent component
     * @return mode of the owned components for consistency in the user experience
     */
    public static Mode propagated(@NotNull Mode mode) {
        return switch (mode) {
            case LIST -> LIST;
            case VIEW, DELETE -> DELETE;
            case EDIT, CREATE, DUPLICATE -> EDIT;
        };
    }

    public String text() {
        return text;
    }

    public boolean readonly() {
        return (this == VIEW) || (this == DELETE);
    }

    public boolean hasForm() {
        return (this != LIST);
    }
}
