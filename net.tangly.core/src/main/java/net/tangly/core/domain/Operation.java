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

package net.tangly.core.domain;

/**
 * Defines the CRUD operations which can be applied on entities.
 */
public enum Operation {
    NONE, VIEW, EDIT, CREATE, DUPLICATE, REPLACE, DELETE;
    public static final String NONE_TEXT = "None";
    public static final String VIEW_TEXT = "View";
    public static final String EDIT_TEXT = "Edit";
    public static final String CREATE_TEXT = "Create";
    public static final String REPLACE_TEXT = "Replace";
    public static final String OK_TEXT = "OK";
    public static final String SAVE_TEXT = "Save";

    public static final String DUPLICATE_TEXT = "Duplicate";
    public static final String DELETE_TEXT = "Delete";

    public boolean readonly() {
        return this == VIEW || this == NONE;
    }

    public String confirmationText() {
        return switch (this) {
            case NONE, VIEW -> OK_TEXT;
            case EDIT, REPLACE -> SAVE_TEXT;
            case DUPLICATE, CREATE -> CREATE_TEXT;
            case DELETE -> DELETE_TEXT;
        };
    }
}
