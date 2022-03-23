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

package net.tangly.core;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

/**
 * Implements the conceptual type of set of related tags, all the same class. The tag type also provides support to convert the text format of a tag into a
 * Java object and to validate acceptable tag values.
 * <p>A tag should be associated at most once with an entity. If multiple values are needed, the tag value should be a comma separated list of all values.
 * The conversion method shall transform the comma separated string into a list of objects if needed.</p>
 *
 * @param namespace namespace of the tags defined through the tag type
 * @param name      name of the tags defined through the tag type
 * @param clazz     class of the type of values stored in the tag
 * @param kind      kind of the tag specifying if a value exists and if it is mandatory
 * @param convert   mapping function between a string to a Java object representation of the string value. if multiple values are supported, the mapping
 *                  function shall return a list of Java objects.
 * @param validate  validation function for the Java representation of the string tag value
 * @param <T>       type of the tags
 */
public record TagType<T>(String namespace, @NotNull String name, @NotNull ValueKinds kind, @NotNull Class<T> clazz, Function<String, T> convert,
                         BiPredicate<TagType<T>, T> validate) {
    /**
     * Indicates if the tag requires no value, an optional value, or a mandatory one.
     */
    public enum ValueKinds {NONE, OPTIONAL, MANDATORY}

    public static TagType<String> ofMandatoryString(String namespace, @NotNull String name) {
        return ofString(namespace, name, ValueKinds.MANDATORY, (tagType, tag) -> true);
    }

    public static TagType<String> ofString(String namespace, @NotNull String name, @NotNull ValueKinds kind,
                                           @NotNull BiPredicate<TagType<String>, String> validate) {
        return of(namespace, name, kind, String.class, UnaryOperator.identity(), validate);
    }

    public static <T> TagType<T> ofMandatory(String namespace, @NotNull String name, Class<T> clazz, Function<String, T> convert,
                                             @NotNull BiPredicate<TagType<T>, T> validate) {
        return of(namespace, name, ValueKinds.MANDATORY, clazz, convert, validate);
    }

    public static <T> TagType<T> ofMandatory(String namespace, @NotNull String name, Class<T> clazz, @NotNull Function<String, T> convert) {
        return of(namespace, name, ValueKinds.MANDATORY, clazz, convert);
    }

    public static <T> TagType<T> of(String namespace, @NotNull String name, @NotNull ValueKinds kind, @NotNull Class<T> clazz,
                                    @NotNull Function<String, T> convert) {
        return new TagType<>(namespace, name, kind, clazz, convert, (tagType, tag) -> true);
    }

    public static <T> TagType<T> of(String namespace, @NotNull String name, @NotNull ValueKinds kind, Class<T> clazz, Function<String, T> convert,
                                    BiPredicate<TagType<T>, T> validate) {
        return new TagType<>(namespace, name, kind, clazz, convert, validate);
    }

    public boolean canHaveValue() {
        return kind != ValueKinds.NONE;
    }

    public boolean mustHaveValue() {
        return kind == ValueKinds.MANDATORY;
    }

    /**
     * Transforms a tag string value into a Java object.
     *
     * @param tag tag which value should be converted
     * @return the Java object representation of the tag value
     */
    public T value(@NotNull Tag tag) {
        return convert().apply(tag.value());
    }

    /**
     * Creates a tag with the given value using the tag type configuration.
     *
     * @param value value of the tag
     * @return new tag instance
     */
    public Tag of(T value) {
        validateValuePresence(value);
        return new Tag(namespace, name, (value != null) ? value.toString() : null);
    }

    public Tag of(String value) {
        validateValuePresence(value);
        return new Tag(namespace, name, value);
    }

    public boolean validate(@NotNull String value) {
        return validate.test(this, convert().apply(value));
    }

    private void validateValuePresence(Object value) {
        if ((kind() == ValueKinds.MANDATORY) && (Objects.isNull(value))) {
            throw new IllegalArgumentException(String.format("value is required for mandatory tag type %s %s", namespace(), name()));
        } else if ((kind() == ValueKinds.NONE) && (Objects.nonNull(value))) {
            throw new IllegalArgumentException(String.format("value is denied for none tag type %s %s", namespace(), name()));
        }
    }
}
