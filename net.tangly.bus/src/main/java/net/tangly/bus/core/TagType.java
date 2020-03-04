/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.bus.core;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Implements the conceptual type of a set of related tags, all of the same class. The tag type also provides support to convert the text format of a
 * tag into a Java object and to validate acceptable tag values.
 *
 * @param <T> type of the tags
 */
public class TagType<T extends Serializable> {
    /**
     * Defines the official geo tag for latitude.
     *
     * @return the tag type for a geographical latitude
     */
    public static TagType<Double> createGeoLatitude() {
        return ofMandatory("geo", "lat", Double.TYPE);
    }

    /**
     * Defines the official geo tag for longitude.
     *
     * @return the tag type for a geographical longitude
     */
    public static TagType<Double> createGeoLongitude() {
        return ofMandatory("geo", "long", Double.TYPE);
    }

    /**
     * Defines the official geo tag for altitude.
     *
     * @return the tag type for a geographical altitude
     */
    public static TagType<Double> createGeoAltitude() {
        return ofMandatory("geo", "alt", Double.TYPE);
    }

    /**
     * Defines the official geo tag for region.
     *
     * @return the tag type for a geographical region
     */
    public static TagType<String> createGeoRegion() {
        return ofMandatory("geo", "region", String.class);
    }

    public enum ValueKinds {NONE, OPTIONAL, MANDATORY}

    private final String namespace;
    private final String name;
    private final ValueKinds kind;
    private final Class<T> clazz;
    private Function<String, T> convert;
    private BiPredicate<TagType<T>, T> validate;

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz, Function<String, T> convert,
                                                                  BiPredicate<TagType<T>, T> validate) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz, convert, validate);
    }

    public static <T extends Serializable> TagType<T> ofMandatory(String namespace, String name, Class<T> clazz, Function<String, T> convert) {
        return new TagType<>(namespace, name, ValueKinds.MANDATORY, clazz, convert, null);
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

    public TagType(String namespace, String name, ValueKinds kind, Class<T> clazz, Function<String, T> convert, BiPredicate<TagType<T>, T> validate) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.kind = Objects.requireNonNull(kind);
        this.clazz = clazz;
        this.convert = convert;
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

    public Class<T> clazz() {
        return clazz;
    }

    public boolean canHaveValue() {
        return kind != ValueKinds.NONE;
    }

    public T getValue(Tag tag) {
        return (convert == null) ? null : convert.apply(tag.value());
    }

    public Tag of(T value) {
        return new Tag(namespace, name, value.toString());
    }

    public Tag of(String value) {
        return new Tag(namespace, name, value);
    }

    public boolean validate(String value) {
        return (validate == null) || validate.test(this, convert.apply(value));
    }
}
