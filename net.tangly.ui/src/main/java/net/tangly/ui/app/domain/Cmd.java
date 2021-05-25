/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.app.domain;

/**
 * Defines the interface for the command pattern. A command is an action mostly executed through the user interface as client. A command is an object whose role
 * is to store all the information required for executing an action, including the method to call, the method arguments, and the object (known as the receiver)
 * that implements the method.
 */
@FunctionalInterface
public interface Cmd {
    /**
     * Executes the command.
     */
    void execute();
}
