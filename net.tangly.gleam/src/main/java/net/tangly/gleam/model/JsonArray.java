/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.gleam.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Defines a field containing an array of entities. The entities can of different types but must have a common ancestor.
 *
 * @param key            name of the property containing the JSON array
 * @param getter         getter of the property - returns a collection of items of type U
 * @param setter         setter used to add an item to the collection mapping the JSON array
 * @param importSelector selector to identify the type of the collection item based on any discriminator in the JSON array
 * @param exportSelector selector to identify the type of the collection item based on any discriminator of the Java object
 * @param <T>            type of the entity owning the field
 * @param <U>            type of the common ancestor of the entities stored in the array
 */
public record JsonArray<T, U>(@NotNull String key, @NotNull Function<T, Collection<U>> getter, @NotNull BiConsumer<T, U> setter,
                              Function<JSONObject, JsonEntity<?>> importSelector, Function<Object, JsonEntity<?>> exportSelector) implements JsonField<T, U> {
    @Override
    public void exports(@NotNull T entity, @NotNull JSONObject object) {
        Collection<U> entities = getter().apply(entity);
        JSONArray items = new JSONArray();
        entities.forEach(o -> {
            JsonEntity jsonEntity = exportSelector.apply(o);
            items.put(jsonEntity.exports(o, entity));
        });
        object.put(key(), items);
    }

    @Override
    public void imports(@NotNull T entity, @NotNull JSONObject object) {
        if (object.has(key())) {
            JSONArray items = object.getJSONArray(key());
            items.forEach(o -> {
                JsonEntity<?> jsonEntity = importSelector.apply((JSONObject) o);
                setter().accept(entity, (U) jsonEntity.imports((JSONObject) o, entity));
            });
        }
    }
}
