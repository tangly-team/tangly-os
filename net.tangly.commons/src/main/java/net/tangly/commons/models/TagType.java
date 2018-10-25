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

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TagType<T> {
    public enum ValueKinds {NONE, OPTIONAL, MANDATORY}

    private String namespace;
    private String name;
    private ValueKinds kind;
    private Class<T> clazz;
    private Function<String, T> convertToObject;
    private BiFunction<TagType<T>, T, Boolean> validate;

    public static <T> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz);
    }

    public static <T> TagType<T> ofOptional(String namespace, String name, Class<T> clazz) {
        return new TagType<>(namespace, name, ValueKinds.OPTIONAL, clazz);
    }

    public TagType(String namespace, String name, ValueKinds kind, Class<T> clazz) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.kind = Objects.requireNonNull(kind);
        this.clazz = clazz;
    }

    public ValueKinds getKind() {
        return kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public <V> V getValue(Tag tag) {
        return (V) clazz.cast(Objects.requireNonNull(tag).getValue());
    }

    public Tag of(T value) {
        return new Tag(namespace, name, value);
    }

    public Tag of(String value) {
        return new Tag(namespace, name, convertToObject.apply(value));
    }

    public boolean validate(String value) {
        return validate.apply(this, convertToObject.apply(value));
    }
}
