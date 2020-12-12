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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jetbrains.annotations.NotNull;

public interface CrudForm<T> {
    /**
     * Define the operation the form can process. The operation has an impact to the behavior of the fields and buttons of the form.
     */
    enum Operation {
        VIEW, UPDATE, CREATE, DELETE, CANCEL, SELECT;

        public boolean isReadWrite() {
            return (this == Operation.CREATE) || (this == Operation.UPDATE) || (this == SELECT);
        }

        public boolean isReadOnly() {
            return (this == Operation.VIEW) || ((this == Operation.DELETE));
        }
    }

    FormLayout createForm(@NotNull Operation operation, T entity);

    default T formCompleted(@NotNull Operation operation, T entity) {
        return entity;
    }

    default void formCancelled(@NotNull Operation operation, T entity) {
    }

    default HorizontalLayout createFormButtons(@NotNull Dialog dialog, @NotNull CrudForm.Operation operation, boolean isCancellable, @NotNull T entity,
                                               @NotNull CrudActionsListener<T> actionsListener) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        Button cancel = new Button("Cancel", event -> {
            formCancelled(CrudForm.Operation.CANCEL, entity);
            dialog.close();
        });
        Button action;
        switch (operation) {
            case VIEW:
                action = new Button("Close");
                actions.add(action);
                action.addClickListener(event -> {
                    formCompleted(CrudForm.Operation.VIEW, entity);
                    dialog.close();
                });
                break;
            case UPDATE:
                action = new Button("Ok");
                if (isCancellable) {
                    actions.add(action, cancel);
                } else {
                    actions.add(action);
                }
                action.addClickListener(event -> {
                    formCompleted(CrudForm.Operation.UPDATE, entity);
                    actionsListener.entityUpdated(entity);
                    dialog.close();
                });
                break;
            case CREATE:
                action = new Button("Create");
                actions.add(action, cancel);
                action.addClickListener(event -> {
                    T created = formCompleted(CrudForm.Operation.CREATE, null);
                    actionsListener.entityAdded(created);
                    dialog.close();
                });
                break;
            case DELETE:
                action = new Button("Delete");
                actions.add(action, cancel);
                action.addClickListener(event -> {
                    formCompleted(CrudForm.Operation.DELETE, entity);
                    actionsListener.entityDeleted(entity);
                    dialog.close();
                });
                break;
            default:
        }
        return actions;
    }
}

