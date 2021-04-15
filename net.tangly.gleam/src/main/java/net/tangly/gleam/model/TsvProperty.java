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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.tangly.commons.lang.Strings;
import net.tangly.commons.lang.exceptions.ThrowingIOExceptionConsumer;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

/**
 * The TSV property defines the mapping between a Java property and one or multiple cells in a TSV file. Two scenarios should be supported. The simple case is
 * the mapping of a Java property to exactly one cell in a TSV file. For example the mapping of a local date property to the textual ISO conform representation
 * in one TSV cell. The more complex case is the mapping of a Java property to multiple cells in a TSV file. For example a Java address object has to be mapped
 * so that each element of the address is stored in a specific cell. Both scenarios are supported through the same abstraction.
 *
 * @param columns   ordered list of columns in the TSV file used to encode the property. Simple fields have one column, complex fields mapped on multiple
 *                  columns have multiple values
 * @param getter    getter function to retrieve the property from a Java entity instance
 * @param setter    optional setter function to set the property of a Java entity instance
 * @param extractor extracts function to read and transform the set of TSV columns into a property value. Factory methods are provided to simplify the
 *                  definition of conversion in the case only one TSV column is used.
 * @param writer    inserts function to transform a property value into a set of TSV columns and write them. Factory methods are provided to simplify the *
 *                  definition of conversion in the case only one TSV column is used.
 * @param <T>       class owning the Java property
 * @param <U>       type of the property
 */
public record TsvProperty<T, U>(List<String> columns, Function<T, U> getter, BiConsumer<T, U> setter, Function<CSVRecord, U> extractor,
                                BiConsumer<U, CSVPrinter> writer) {

    public static final Function<String, BigDecimal> CONVERT_BIG_DECIMAL_FROM = e -> (e == null) ? BigDecimal.ZERO : new BigDecimal(e);
    public static final Function<String, LocalDate> CONVERT_DATE_FROM = e -> (e != null) ? LocalDate.parse(e) : null;
    public static final Function<String, LocalDateTime> CONVERT_DATETIME_FROM = e -> (e != null) ? LocalDateTime.parse(e) : null;

    public static <T, U> TsvProperty<T, U> of(@NotNull TsvEntity<U> entity, Function<T, U> getter, BiConsumer<T, U> setter) {
        List<String> columns = entity.fields().stream().map(e -> e.columns().get(0)).toList();
        return of(columns, getter, setter, entity::imports, entity::exports);
    }

    /**
     * Define a property mapped to one column without any transformation steps between property type and string representation.
     *
     * @param column column in which the property will be stored in the TSV record
     * @param getter getter method to read the property from the entity
     * @param setter setter method to write the property into the entity
     * @param <T>    entity type
     * @return new TSV property
     */
    public static <T> TsvProperty<T, String> ofString(@NotNull String column, Function<T, String> getter, BiConsumer<T, String> setter) {
        return of(column, getter, setter, v -> v, u -> u);
    }

    public static <T> TsvProperty<T, LocalDate> ofDate(@NotNull String column, Function<T, LocalDate> getter, BiConsumer<T, LocalDate> setter) {
        return of(column, getter, setter, v -> (v != null) ? LocalDate.parse(v) : null, u -> u);
    }

    public static <T> TsvProperty<T, Integer> ofInt(@NotNull String column, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
        return of(column, getter, setter, v -> (v == null) ? 0 : Integer.parseInt(v), u -> u);
    }

    public static <T> TsvProperty<T, Long> ofLong(@NotNull String column, Function<T, Long> getter, BiConsumer<T, Long> setter) {
        return of(column, getter, setter, v -> (v == null) ? 0 : Long.parseLong(v), u -> u);
    }

    public static <T> TsvProperty<T, BigDecimal> ofBigDecimal(@NotNull String column, Function<T, BigDecimal> getter, BiConsumer<T, BigDecimal> setter) {
        return of(column, getter, setter, v -> (v == null) ? BigDecimal.ZERO : new BigDecimal(v), BigDecimal::toPlainString);
    }

    public static <T, U extends Enum<U>> TsvProperty<T, U> ofEnum(@NotNull Class<U> clazz, @NotNull String column, Function<T, U> getter,
                                                                  BiConsumer<T, U> setter) {
        return of(column, getter, setter, v -> Enum.valueOf(clazz, v.toLowerCase()), U::name);
    }

    /**
     * Define a property mapped to one column with  a transformation step from the string representation to the property type.
     *
     * @param column      column in which the property will be stored in the TSV record
     * @param getter      getter method to read the property from the entity
     * @param setter      setter method to write the property into the entity
     * @param convertFrom transforms a string into the property type representation
     * @param <T>         entity type
     * @param <U>         property type
     * @return new TSV property
     */
    public static <T, U> TsvProperty<T, U> of(@NotNull String column, Function<T, U> getter, BiConsumer<T, U> setter, Function<String, U> convertFrom) {
        return of(column, getter, setter, convertFrom, u -> u);
    }

    /**
     * Define a property mapped to one column with  transformations between string and property type.
     *
     * @param column      column in which the property will be stored in the TSV record
     * @param getter      getter method to read the property from the entity
     * @param setter      setter method to write the property into the entity
     * @param convertFrom transforms a string into the property type representation
     * @param convertTo   transforms a property type representation into a string
     * @param <T>         entity type
     * @param <U>         property type
     * @return new TSV property
     */
    public static <T, U> TsvProperty<T, U> of(@NotNull String column, Function<T, U> getter, BiConsumer<T, U> setter, @NotNull Function<String, U> convertFrom,
                                              @NotNull Function<U, Object> convertTo) {
        Objects.requireNonNull(convertFrom);
        Objects.requireNonNull(convertTo);
        return of(List.of(column), getter, setter, record -> convertFrom.apply(Strings.emptyToNull(record.get(column))),
            (property, out) -> print(out, convertTo.apply(property)));
    }

    public static <T, U> TsvProperty<T, U> of(@NotNull List<String> columns, Function<T, U> getter, BiConsumer<T, U> setter,
                                              @NotNull Function<CSVRecord, U> extractor, @NotNull BiConsumer<U, CSVPrinter> writer) {
        return new TsvProperty<>(columns, getter, setter, extractor, writer);
    }

    /**
     * Wrapper function to transformed checked exception to standard unchecked exception for usage in lambda expressions
     *
     * @param out   printer to write values
     * @param value value to be written
     */
    public static void print(@NotNull CSVPrinter out, Object value) {
        ThrowingIOExceptionConsumer.of(out::print).accept(value);
    }

    /**
     * Import the TSV value and set the associated property after an optional conversion.
     *
     * @param entity entity which property will be imported and set
     * @param record record containing the TSV values
     * @see TsvProperty#exports(Object, CSVPrinter)
     */
    public void imports(@NotNull T entity, @NotNull CSVRecord record) {
        U property = extractor.apply(record);
        setter.accept(entity, property);
    }

    /**
     * Export the TSV value from the associated property after an optional conversion.
     *
     * @param entity entity which property will be exported as TSV value
     * @param out    printer to write the TSV value(s)
     * @see TsvProperty#imports(Object, CSVRecord)
     */
    public void exports(@NotNull T entity, @NotNull CSVPrinter out) {
        U property = getter.apply(entity);
        writer.accept(property, out);
    }
}
