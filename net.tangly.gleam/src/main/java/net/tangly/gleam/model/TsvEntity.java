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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

/**
 * Defines an entity mapped to a row of TSV values written to a TSV file.
 *
 * @param fields  fields of the entity with the mapping rules
 * @param factory factory method to create a new entity instance
 * @param <T>     class of the entity
 */
public record TsvEntity<T>(Class<T> clazz, List<TsvProperty<T, ?>> fields, Supplier<T> factory, Function<CSVRecord, T> imports,
                           BiConsumer<T, CSVPrinter> exports) {
    public static <T> TsvEntity<T> of(Class<T> clazz, List<TsvProperty<T, ?>> properties, Supplier<T> factory) {
        return new TsvEntity<>(clazz, properties, factory, null, null);
    }

    public static <T> TsvEntity<T> of(Class<T> clazz, List<TsvProperty<T, ?>> properties, Function<CSVRecord, T> imports) {
        return new TsvEntity<>(clazz, properties, null, imports, null);
    }

    /**
     * Return the ordered list of column header names for the TSV record header.
     *
     * @return ordered list of column header names for the TSV record header
     */
    public List<String> headers() {
        return fields().stream().flatMap(e -> e.columns().stream()).toList();
    }

    /**
     * Export an entity as a row in a TSV file. Upon completion of the export the caller shall call. If defined the imports lambda is executed, otherwise the
     * import lambdas for all fields are executed.
     *
     * @param entity  entity to be exported as TSV record
     * @param printer printer to write TSV record
     */
    public void exports(@NotNull T entity, @NotNull CSVPrinter printer) {
        if (exports() != null) {
            exports().accept(entity, printer);
        } else {
            fields().forEach(property -> property.exports(entity, printer));
        }
    }

    /**
     * Import an entity from a TSV record. First if defined the imports lambda is executed otherwise the factory is used to create a new Java domain object.
     * When all fields with a defined setter will have their imports lambda executed.
     *
     * @param record TSV record containing the TSV representation of the entity
     * @return entity base on the TSV record
     */
    public T imports(@NotNull CSVRecord record) {
        T entity;
        if (imports() != null) {
            entity = imports().apply(record);
        } else {
            entity = factory.get();
        }
        fields().stream().filter(o -> o.setter() != null).forEach(property -> property.imports(entity, record));
        return entity;
    }
}
