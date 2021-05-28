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

package net.tangly.gleam.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/**
 * Defines a JSON property mapping a Java class to a JSON entity property or mapping a simple property to a simple JSON property.
 * <p>The API defines two sets of services</p>
 * <ul>
 *     <li>the first protocol defines how the property of type U is converted to and from a JSON representation. The JSON key under which the
 *     JSON representation is stored in the JSON structure is also accessible. This protocol is defined in {@link JsonField}</li>
 *     <li>the second protocol defines how the JSON representation can be written to or read from the Java instance.</li>
 * </ul>
 *
 * @param key      name of the property containing the JSON array
 * @param getter   getter of the property - returns an item of type U from an object of type T
 * @param setter   setter of the property of type U to the object of type T
 * @param extracts conversion function between a JSON object and a Java object of type U
 * @param inserts  conversion function between a Java object of type U and a JSON object type
 * @param <T>      type of the entity owning the field
 * @param <U>      type of the property
 */
public record JsonProperty<T, U>(@NotNull String key, @NotNull Function<T, U> getter, @NotNull BiConsumer<T, U> setter,
                                 @NotNull Function<JSONObject, U> extracts, @NotNull BiFunction<U, JSONObject, JSONObject> inserts) implements JsonField<T, U> {

    public static <T> JsonProperty<T, String> ofString(@NotNull String key, Function<T, String> getter, BiConsumer<T, String> setter) {
        return of(key, getter, setter, o -> o.has(key) ? o.getString(key) : null, (u, o) -> o.put(key, u));
    }

    public static <T> JsonProperty<T, Integer> ofInt(@NotNull String key, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
        return of(key, getter, setter, o -> o.has(key) ? o.getInt(key) : null, (u, o) -> o.put(key, u));
    }

    public static <T> JsonProperty<T, Long> ofLong(@NotNull String key, Function<T, Long> getter, BiConsumer<T, Long> setter) {
        return of(key, getter, setter, o -> o.has(key) ? o.getLong(key) : null, (u, o) -> o.put(key, u));
    }

    public static <T> JsonProperty<T, BigDecimal> ofBigDecimal(@NotNull String key, Function<T, BigDecimal> getter, BiConsumer<T, BigDecimal> setter) {
        return of(key, getter, setter, o -> o.has(key) ? o.getBigDecimal(key) : null, (u, o) -> o.put(key, u.toPlainString()));
    }

    public static <T> JsonProperty<T, LocalDate> ofLocalDate(@NotNull String key, Function<T, LocalDate> getter, BiConsumer<T, LocalDate> setter) {
        return of(key, getter, setter, o -> hasString(o, key) ? LocalDate.parse(o.getString(key)) : null,
            (u, o) -> o.put(key, u.toString()));
    }

    public static <T> JsonProperty<T, Currency> ofCurrency(@NotNull String key, Function<T, Currency> getter, BiConsumer<T, Currency> setter) {
        return of(key, getter, setter, o -> hasString(o, key) ? Currency.getInstance(o.getString(key)) : null,
            (u, o) -> o.put(key, u.getCurrencyCode()));
    }

    public static <T> JsonProperty<T, Locale> ofLocale(@NotNull String key, Function<T, Locale> getter, BiConsumer<T, Locale> setter) {
        return of(key, getter, setter, o -> hasString(o, key) ? Locale.forLanguageTag(o.getString(key)) : null,
            (u, o) -> o.put(key, u.toLanguageTag()));
    }

    /**
     * Create a JSON property of type U
     *
     * @param property name of the property
     * @param getter   getter of the property
     * @param setter   setter of the property
     * @param entity   JSON entity describing the JSON representation of type U
     * @param <T>      type of the entity owning the property
     * @param <U>      type of the Java property
     * @return new JSON property
     */
    public static <T, U> JsonProperty<T, U> ofType(@NotNull String property, Function<T, U> getter, BiConsumer<T, U> setter, JsonEntity<U> entity) {
        return of(property, getter, setter, o -> entity.imports(o.getJSONObject(property)), (u, o) -> o.put(property, entity.exports(u)));
    }

    public static <T, U> JsonProperty<T, U> of(@NotNull String key, @NotNull Function<T, U> getter, BiConsumer<T, U> setter,
                                               @NotNull Function<JSONObject, U> extracts, @NotNull BiFunction<U, JSONObject, JSONObject> inserts) {
        return new JsonProperty<>(key, getter, setter, extracts, inserts);
    }

    /**
     * Import the JSON value and transform it to the type of the associated property aftet an optional conversion. The implementation has the knowledge how to
     * extract the values from the JSON object based of the value type used.
     *
     * @param object JSON object containing the values to convert
     * @return Java property value associated with the JSON object
     * @see JsonProperty#convertToJson(Object, JSONObject)
     */
    public U convertFromJson(@NotNull JSONObject object) {
        return extracts.apply(object);
    }

    /**
     * Export the JSON value from the associated property after an optional conversion. Multiple values or whole JSON object structures can be written if the
     * object is a complex one. The implementation has the knowledge how to write the values from the JSON object based of the value type used.
     *
     * @param property to transform into a JSON object
     * @param object   JSON object containing the values to convert
     * @return JSON object containing the converted values
     * @see JsonProperty#convertFromJson(JSONObject)
     */
    public JSONObject convertToJson(@NotNull U property, @NotNull JSONObject object) {
        return object;
    }

    @Override
    public void imports(@NotNull T entity, @NotNull JSONObject object) {
        U property = extracts.apply(object);
        setter().accept(entity, property);
    }

    @Override
    public void exports(@NotNull T entity, @NotNull JSONObject object) {
        U property = getter().apply(entity);
        if (property != null) {
            inserts().apply(property, object);
        }
    }

    private static boolean hasString(@NotNull JSONObject object, @NotNull String key) {
        return (object.has(key) && object.getString(key) != null);
    }
}
