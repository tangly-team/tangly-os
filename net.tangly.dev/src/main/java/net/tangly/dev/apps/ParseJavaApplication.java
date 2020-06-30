/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.dev.apps;

import net.tangly.dev.model.Clazz;
import net.tangly.dev.model.Packages;

public class ParseJavaApplication {

    /**
     * A module is identified as [module]/src/main/java/**. The java source files are located in the module under [module]/src/main/java, the java
     * test source files are located in the module under [module]/src/test/java. The package name is identified as
     * [module]/src/main/java/[package]/[class].java
     */
    public void arseModules() {

    }

    public Module findOrCreateModule(String name) {
        return null;
    }

    public Packages findOrCreatePackage(String name) {
        return null;
    }

    public Clazz findOrCreateClass(String name) {
        return null;
    }
}
