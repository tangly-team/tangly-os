/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TagType<T extends Serializable> {
    public enum ValueKinds {NONE, OPTIONAL, MANDATORY}

    private final String namespace;
    private final String name;
    private final ValueKinds kind;
    private final Class<T> clazz;
    private Function<String, T> convertToObject;
    private BiFunction<TagType<T>, T, Boolean> validate;

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz,
                                                                  Function<String, T> convertToObject, BiFunction<TagType<T>, T, Boolean> validate) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz, convertToObject, validate);
    }

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz,
                                                                  Function<String, T> convertToObject) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz, convertToObject, null);
    }

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz);
    }

    public static <T extends Serializable> TagType<T> ofOptional(String namespace, String name, Class<T> clazz) {
        return new TagType<>(namespace, name, ValueKinds.OPTIONAL, clazz);
    }

    public TagType(String namespace, String name, ValueKinds kind, Class<T> clazz) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.kind = Objects.requireNonNull(kind);
        this.clazz = clazz;
    }

    public TagType(String namespace, String name, ValueKinds kind, Class<T> clazz, Function<String, T> convertToObject, BiFunction<TagType<T>, T,
            Boolean> validate) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.kind = Objects.requireNonNull(kind);
        this.clazz = clazz;
        this.convertToObject = convertToObject;
        this.validate = validate;
    }

    public ValueKinds kind() {
        return kind;
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return name;
    }

    public T getValue(Tag tag) {
        return (convertToObject == null) ? null : convertToObject.apply(tag.value());
    }

    public Tag of(T value) {
        return new Tag(namespace, name, value.toString());
    }

    public Tag of(String value) {
        return new Tag(namespace, name, value);
    }

    public boolean validate(String value) {
        return (validate == null) || validate.apply(this, convertToObject.apply(value));
    }
}
