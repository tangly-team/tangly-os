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

package net.tangly.bdd.engine;

/**
 * JSON field identifiers used to generate a JSON representation of stories and features.
 */
public final class Constants {
    public static final String EXT = ".json";
    public static final String BDD_REPORTS_FOLDER = "bdd-reports";

    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";

    public static final String PACKAGE_NAME = "package";
    public static final String CLASS_NAME = "class";
    public static final String METHOD_NAME = "method";

    public static final String SCENARIOS = "scenarios";
    public static final String STORIES = "stories";

    public static final String GIVEN = "given";
    public static final String WHEN = "when";
    public static final String THEN = "then";
    public static final String AND = "and";
    public static final String TEXT = "text";

    /**
     * Private constructor of an utility class.
     */
    private Constants() {
    }
}
