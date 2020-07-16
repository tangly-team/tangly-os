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

package net.tangly.bus.core;

import java.io.Serializable;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

/**
 * Implements the conceptual type of a set of related tags, all of the same class. The tag type also provides support to convertTo the text format of a tag into
 * a Java object and to validate acceptable tag values.
 *
 * @param namespace namespace of the tags defined through the tag type
 * @param name      name of the tags defined through the tag type
 * @param clazz     type of the values stored in the tag
 * @param <T>       type of the tags
 */
public record TagType<T extends Serializable>(String namespace, String name, ValueKinds kind, Class<T> clazz, Function<String, T> convertTo,
                                              BiPredicate<TagType<T>, T> validate) {
    public enum ValueKinds {NONE, OPTIONAL, MANDATORY}

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz, Function<String, T> convertTo,
                                                                  BiPredicate<TagType<T>, T> validate) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz, convertTo, validate);
    }

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz, Function<String, T> convertTo) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz, convertTo, null);
    }

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz, null, null);
    }

    public static <T extends Serializable> TagType<T> ofOptional(String namespace, String name, Class<T> clazz, Function<String, T> convertTo,
                                                                 BiPredicate<TagType<T>, T> validate) {
        return new TagType<>(namespace, name, ValueKinds.OPTIONAL, clazz, convertTo, validate);
    }

    public static <T extends Serializable> TagType<T> ofOptional(String namespace, String name, Class<T> clazz) {
        return new TagType<>(namespace, name, ValueKinds.OPTIONAL, clazz, null, null);
    }

    public boolean canHaveValue() {
        return kind != ValueKinds.NONE;
    }

    public T getValue(@NotNull Tag tag) {
        return (convertTo() == null) ? null : convertTo().apply(tag.value());
    }

    public Tag of(@NotNull T value) {
        return new Tag(namespace, name, value.toString());
    }

    public Tag of(String value) {
        return new Tag(namespace, name, value);
    }

    public boolean validate(@NotNull String value) {
        return (validate == null) || validate.test(this, convertTo().apply(value));
    }
}
