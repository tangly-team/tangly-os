/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.gleam.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines a mapping between JSON entity and a Java entity. We have an isomorphic structure of two graphs, meaning Java entities can contain other entities and
 * JSON entities can contain the associated JSON entities mapping these Java entities.
 * <ul>
 *   <li>A regular Java class is mapped. The property declarations are used to map between Java instance and JSON object.</li>
 *   <li>A Java class is inferred from some JSON fields and lookup in the data model. Therefore the import function is specific to the business
 *      logic of the domain entity. The export function uses the regular approach.</li>
 *   <li>A Java record is used as Java domain model representation. Therefore the import function is record specific because no setter are
 *      available. The export function uses the regular approach.</li>
 *  </ul>
 *  <p>The BiFunction lambdas for imports and exports transformations are needed when the transformation process needs the context of the owning entity to
 *  perform semantic checks or mappings. If no context is required simply use the factory method providing Function lambdas.</p>
 *
 * @param properties the fields of the JSON entity
 * @param factory    factory to create a new Java object based on the JSON entity
 * @param imports    optional import function to translate a JSON object into a Java object
 * @param exports    optional export function to translate a Java object into a JSON representation
 * @param <T>        type of Java entity to map with a JSON representation
 */
public record JsonEntity<T>(List<JsonField<T, ?>> properties, Supplier<T> factory, BiFunction<JSONObject, Object, T> imports,
                            BiFunction<T, Object, JSONObject> exports) {
    /**
     * Defines a factory method for a regular Java class mapped to a JSON type.
     *
     * @param properties fields of the Java class to be exported to JSON and imported From JSON.
     * @param factory    factory method to create a new Java class instance, often the default constructor
     * @param <T>        type to be converted to JSON representation
     * @return JSON entity defining the transformation process
     */
    public static <T> JsonEntity<T> of(@NotNull List<JsonField<T, ?>> properties, @NotNull Supplier<T> factory) {
        return of(properties, factory, null, (Function<T, JSONObject>) null);
    }

    /**
     * Defines a factory method for a Java record mapped to a JSON type. The imports block is custom to handle the construction of a record, the export uses regular mechanisms
     * through the declaration of the fields for the Java class.
     *
     * @param properties fields of the Java class to be exported to JSON and imported From JSON.
     * @param imports    import function transforming a JSON object into a Java record
     * @param <T>        record type to be converted to JSON representation
     * @return JSON entity defining the transformation process
     */
    public static <T> JsonEntity<T> of(@NotNull List<JsonField<T, ?>> properties, @NotNull Function<JSONObject, T> imports) {
        return of(properties, null, imports, null);
    }

    public static <T> JsonEntity<T> of(BiFunction<JSONObject, Object, T> imports, BiFunction<T, Object, JSONObject> exports) {
        return of(Collections.emptyList(), null, imports, exports);
    }

    /**
     * Defines a factory method for a context less transformation process.
     *
     * @param properties fields of the Java class to be exported to JSON and imported From JSON.
     * @param factory    factory method to create a new Java class instance, often the default constructor
     * @param imports    import function transforming a JSON object into a Java instance
     * @param exports    export function transforming a Java instance into a JSON object
     * @param <T>        type of the Java instances
     * @return JSON entity defining the transformation process
     */
    public static <T> JsonEntity<T> of(List<JsonField<T, ?>> properties, Supplier<T> factory, Function<JSONObject, T> imports,
                                       Function<T, JSONObject> exports) {
        return of(properties, factory, (imports != null) ? (object, _) -> imports.apply(object) : null,
            (exports != null) ? (entity, _) -> exports.apply(entity) : null);
    }

    /**
     * Defines a factory method for transformation process with the context of the owning entity
     *
     * @param properties fields of the Java class to be exported to JSON and imported From JSON.
     * @param factory    factory method to create a new Java class instance, often the default constructor
     * @param imports    import function transforming a JSON object into a Java instance, parameters are the JSON object, the object owning the transformed Java
     *                   entity and the Java object as return value.
     * @param exports    export function transforming a Java instance into a JSON object. The parameters are the Java entity to be transformed in a JSON
     *                   representation, the object owning the Java entity and the JSON object of the entity JSON representation as return value.
     * @param <T>        type of the Java instances
     * @return JSON entity defining the transformation process
     */
    public static <T> JsonEntity<T> of(List<JsonField<T, ?>> properties, Supplier<T> factory, BiFunction<JSONObject, Object, T> imports,
                                       BiFunction<T, Object, JSONObject> exports) {
        return new JsonEntity<>(properties, factory, imports, exports);
    }

    /**
     * Export an entity as a JSON entity.
     *
     * @param entity entity to be exported as JSON object
     * @return JSON representation of the entity
     */
    public JSONObject exports(@NotNull T entity) {
        return exports(entity, null);
    }

    /**
     * Export an entity as a JSON entity.
     *
     * @param entity  entity to be exported as JSON object
     * @param context context of the transformed entity, meaning the owning Java instance
     * @return JSON representation of the entity
     */
    public JSONObject exports(@NotNull T entity, Object context) {
        if (exports() != null) {
            return exports().apply(entity, context);
        } else {
            JSONObject object = new JSONObject();
            properties().forEach(property -> property.exports(entity, object));
            return object;
        }
    }

    /**
     * Imports an entity from a JSON object.
     *
     * @param object JSON representation of the entity
     * @return entity created based on the JSON object
     */
    public T imports(@NotNull JSONObject object) {
        return imports(object, null);
    }

    /**
     * Imports an entity from a JSON object.
     *
     * @param object  JSON representation of the entity
     * @param context context of the transformed entity, meaning the owning Java instance
     * @return entity created based on the JSON object
     */
    public T imports(@NotNull JSONObject object, Object context) {
        if (imports() != null) {
            return imports().apply(object, context);
        } else {
            T entity = factory.get();
            properties().forEach(property -> property.imports(entity, object));
            return entity;
        }
    }
}
