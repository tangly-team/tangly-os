/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.Collections;
import java.util.Set;

/**
 * The interface defines the abstraction for the command pattern. A command is an action mostly executed through the user interface as client. A command is an object whose role
 * is to store all the information required for executing an action, including the method to call, the method arguments, and the object (known as the receiver) that implements
 * the method.
 */
@FunctionalInterface
public interface Cmd {
    /**
     * Executes the command.
     */
    void execute();

    static Dialog createDialog(String width, FormLayout form) {
        Dialog dialog = new Dialog();
        dialog.setWidth(width);
        dialog.setResizable(true);
        dialog.add(form);
        return dialog;
    }

    /**
     * Indicates if the command is enabled or not. A command availability is dependent on the application and roles the user has.
     *
     * @return true if enabled otherwise false
     */
    default boolean isAllowed() {
        return true;
    }

    /**
     * Returns the roles requested to execute the command.
     *
     * @return roles allowed to execute the command
     */
    default Set<String> roles() {
        return Collections.emptySet();
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
