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

import net.tangly.commons.lang.functional.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/**
 * Defines a JSON property mapping a Java class to a JSON entity property or mapping a simple property to a simple JSON property.
 *
 * @param property name of the property containing the JSON array
 * @param getter   getter of the property - returns an item of type U
 * @param setter   setter of the property
 * @param extracts conversion function between a JSON object and a Java object of type U
 * @param inserts  conversion function between a Java object of type U and a JSON object type
 * @param <T>      type of the entity owning the field
 * @param <U>      type of the property
 */
public record JsonProperty<T, U>(@NotNull String property, @NotNull Function<T, U> getter, @NotNull BiConsumer<T, U> setter,
                                 @NotNull BiFunction<JSONObject, T, U> extracts, @NotNull TriFunction<U, T, JSONObject, JSONObject> inserts)
        implements JsonField<T, U> {

    public static <T> JsonProperty<T, String> ofString(String property, Function<T, String> getter, BiConsumer<T, String> setter) {
        return of(property, getter, setter, o -> JsonField.get(property, o), (String u, JSONObject o) -> o.put(property, u));
    }

    public static <T> JsonProperty<T, Integer> ofInt(String property, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
        return of(property, getter, setter, o -> o.has(property) ? o.getInt(property) : null, (u, o) -> o.put(property, u));
    }

    public static <T> JsonProperty<T, Long> ofLong(String property, Function<T, Long> getter, BiConsumer<T, Long> setter) {
        return of(property, getter, setter, o -> o.has(property) ? o.getLong(property) : null, (u, o) -> o.put(property, u));
    }

    public static <T> JsonProperty<T, BigDecimal> ofBigDecimal(String property, Function<T, BigDecimal> getter, BiConsumer<T, BigDecimal> setter) {
        return of(property, getter, setter, o -> o.has(property) ? o.getBigDecimal(property) : null, (u, o) -> o.put(property, u.toPlainString()));
    }

    public static <T> JsonProperty<T, LocalDate> ofLocalDate(String property, Function<T, LocalDate> getter, BiConsumer<T, LocalDate> setter) {
        return of(property, getter, setter, o -> (JsonField.get(property, o) != null) ? LocalDate.parse(o.getString(property)) : null,
                (u, o) -> o.put(property, u));
    }

    public static <T> JsonProperty<T, Currency> ofCurrency(String property, Function<T, Currency> getter, BiConsumer<T, Currency> setter) {
        return of(property, getter, setter, o -> (JsonField.get(property, o) != null) ? Currency.getInstance(o.getString(property)) : null,
                (u, o) -> o.put(property, u));
    }

    public static <T> JsonProperty<T, Locale> ofLocale(String property, Function<T, Locale> getter, BiConsumer<T, Locale> setter) {
        return of(property, getter, setter, o -> (JsonField.get(property, o) != null) ? Locale.forLanguageTag(o.getString(property)) : null,
                (u, o) -> o.put(property, u.toLanguageTag()));
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
    public static <T, U> JsonProperty<T, U> ofType(String property, Function<T, U> getter, BiConsumer<T, U> setter, JsonEntity<U> entity) {
        return of(property, getter, setter, o -> entity.imports(o.getJSONObject(property)), (u, o) -> o.put(property, entity.exports(u)));
    }

    public static <T, U> JsonProperty<T, U> of(@NotNull String property, @NotNull Function<T, U> getter, @NotNull BiConsumer<T, U> setter,
                                               @NotNull Function<JSONObject, U> extracts, @NotNull BiFunction<U, JSONObject, JSONObject> inserts) {
        return of(property, getter, setter, (o, e) -> extracts.apply(o), (u, e, o) -> inserts.apply(u, o));
    }

    public static <T, U> JsonProperty<T, U> of(@NotNull String property, @NotNull Function<T, U> getter, @NotNull BiConsumer<T, U> setter,
                                               @NotNull BiFunction<JSONObject, T, U> extracts, @NotNull TriFunction<U, T, JSONObject, JSONObject> inserts) {
        return new JsonProperty<>(property, getter, setter, extracts, inserts);
    }

    @Override
    public void imports(@NotNull T entity, @NotNull JSONObject object) {
        U property = extracts.apply(object, entity);
        setter().accept(entity, property);
    }

    @Override
    public void exports(@NotNull T entity, @NotNull JSONObject object) {
        U property = getter().apply(entity);
        if (property != null) {
            inserts().apply(property, entity, object);
        }
    }
}
