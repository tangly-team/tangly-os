/*
 * Copyright 2006-2021 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.gleam.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/**
 * Defines the abstraction of a mapping between a Java property - simple type, another class mapped to a JSON entity, or a collection mapped to a JSON array - and a JSON property,
 * type, or array.
 *
 * @param <T> type of the entity owning the field
 * @param <U> type of the property
 */
public sealed interface JsonField<T, U> permits JsonProperty, JsonArray {
    /**
     * Import the JSON value and set the associated property after an optional conversion. Multiple JSON values can be used if the object is complex.
     *
     * @param entity entity which property will be imported and set
     * @param object JSON object containing the values
     * @see JsonProperty#exports(Object, JSONObject)
     */
    void imports(@NotNull T entity, @NotNull JSONObject object);

    /**
     * Export the JSON value from the associated property after an optional conversion. Multiple JSON values can be written if the object is a complex one.
     *
     * @param entity entity which property will be exported as TSV value
     * @param object JSON object to write the TSV value(s)
     * @see JsonProperty#imports(Object, JSONObject)
     */
    void exports(@NotNull T entity, @NotNull JSONObject object);

    /**
     * Returns the JSON property key name storing the property in a JSON structure.
     *
     * @return key of the property
     */
    String key();

    static String string(JSONObject object, String key) {
        return object.has(key) ? object.getString(key) : null;
    }
}
