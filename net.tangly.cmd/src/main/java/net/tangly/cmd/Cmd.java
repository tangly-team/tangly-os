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

package net.tangly.cmd;

/**
 * A command is used to request a service or data from a domain. It defines the interface of the domain to its customers.
 */
public interface Cmd {
    /**
     * Return the group of the command. The group is used to group commands for a specific interpreter
     *
     * @return group of the command.
     */
    String group();

    /**
     * Human-readable identifier of the command instruction. Programmatically, the command instruction is identified through its class.
     *
     * @return command instruction identifier
     */
    String name();

    /**
     * Return true if the command returns a value. This is typical for synchronous queries.
     *
     * @return flag indicating if the command returns a value
     */
    boolean hasAnswer();
}
