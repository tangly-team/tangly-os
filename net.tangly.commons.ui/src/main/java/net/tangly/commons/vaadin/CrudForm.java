/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;

public interface CrudForm<T> {
    public enum Operation {
        VIEW, UPDATE, CREATE, DELETE, CANCEL;

        static boolean isReadWrite(@NotNull Operation operation) {
            return (operation == Operation.CREATE) || ((operation == Operation.UPDATE));
        }

        static boolean isReadOnly(@NotNull Operation operation) {
            return (operation == Operation.VIEW) || ((operation == Operation.DELETE));
        }
    }

    static TextField createTextField(String label, String placeholder, boolean readonly, boolean enabled) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setReadOnly(readonly);
        field.setEnabled(enabled);
        return field;
    }

    FormLayout createForm(Operation operation, T entity);

    default T formCompleted(Operation operation, T entity) {
        return entity;
    }

    default void formCancelled(Operation operation, T entity) {
    }
}
