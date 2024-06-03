/*
 * Copyright 2022-2024 Marcel Baumann
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

import net.tangly.core.domain.AccessRightsCode;
import org.jetbrains.annotations.NotNull;

/**
 * Define the modes of the item view.
 * <dl>
 *     <dt>LIST</dt><dd>The list mode displays a read-only grid of entities without any default menu items.</dd>
 *     <dt>VIEW</dt><dd>The view mode displays a read-only grid of entities with a details form and associated menu item.</dd>
 *     <dt>EDIT</dt><dd>The edit mode displays a grid of entities with a details form for edition and creation. The edit mode supports edition, creation, and duplication through
 *     menu items. </dd>
 *     <dt>DELETE</dt><dd>The delete mode displays a grid of entities with a read-only details form for deletion.</dd>
 * </dl>
 */
public enum Mode {
    LIST(Mode.LIST_TEXT), VIEW(Mode.VIEW_TEXT), EDITABLE(Mode.EDIT_TEXT);

    public static final String LIST_TEXT = "List";
    public static final String VIEW_TEXT = "View";
    public static final String EDIT_TEXT = "Edit";

    private final String text;

    Mode(@NotNull String text) {
        this.text = text;
    }

    public static Mode mode(AccessRightsCode right) {
        return switch (right) {
            case null -> Mode.LIST;
            case none -> Mode.LIST;
            case readonlyUser -> Mode.VIEW;
            case appAdmin, restrictedUser, user, domainAdmin -> Mode.EDITABLE;
        };
    }

    public String text() {
        return text;
    }

    public boolean readonly() {
        return (this == LIST) || (this == VIEW);
    }
}
