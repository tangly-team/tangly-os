/*
 * Copyright 2006-2019 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.di;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
/**
 * Defines a set of bindings for a software subsystems. The module is used to defined dependency injections rules for a set of components in a
 * declarative way. The application can injects all needed modules into an injector to have a wholly configured injector.
 */ public interface Module {
    /**
     * Configures the injector with the bindings defined in the method.
     *
     * @param injector injector to configure
     */
    void configure(@NotNull Injector injector);
}
