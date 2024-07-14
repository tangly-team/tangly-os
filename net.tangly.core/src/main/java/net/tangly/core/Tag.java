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

package net.tangly.core;


import net.tangly.commons.lang.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The immutable class tag models a tag with a name and a format. Tags provide a powerful approach for multidimensional classifications of values. All fields of a tag are strings.
 * We provide the tag type to support conversions from string values to Java objects.
 * <p>A tag without a value is equivalent ot a label.</p>
 *
 * @param namespace the optional namespace in which the tag is defined
 * @param name      the name of the tag
 * @param value     the optional value of the tag
 */
public record Tag(String namespace, @NotNull String name, String value) {
    public Tag {
        Objects.requireNonNull(name);
    }

    /**
     * Transforms a collection of tags into their canonical textual representation.
     *
     * @param tags tags to be transformed
     * @return textual representation of the tags
     * @see Tag#toTags(String)
     */
    public static String text(@NotNull Collection<Tag> tags) {
        return tags.isEmpty() ? null : tags.stream().map(Tag::text).collect(Collectors.joining(";"));
    }

    /**
     * Transforms raw tags textual representation into a set of tags
     *
     * @param rawTags textual representation of tags
     * @return set of the tag instances
     * @see Tag#text(Collection)
     */
    public static Set<Tag> toTags(String rawTags) {
        return Strings.isNullOrEmpty(rawTags) ? new HashSet<>() : Arrays.stream(rawTags.split(";")).map(Tag::parse).collect(Collectors.toSet());
    }

    public static String namespace(@NotNull String tag) {
        return tag.contains(":") ? tag.substring(0, tag.indexOf(':')) : null;
    }

    public static String name(@NotNull String tag) {
        return tag.substring((tag.contains(":") ? tag.indexOf(':') + 1 : 0), (tag.contains("=")) ? tag.indexOf('=') : tag.length());
    }

    public static String value(@NotNull String tag) {
        return tag.contains("=") ? tag.substring(tag.indexOf('=') + 1) : null;
    }

    public static Tag parse(String tag) {
        Objects.requireNonNull(tag);
        return of(namespace(tag), name(tag), value(tag));
    }

    public static Tag ofEmpty(String namespace, @NotNull String name) {
        return of(namespace, name, null);
    }

    public static Tag ofEmpty(@NotNull String name) {
        return of(null, name, null);
    }

    public static Tag of(@NotNull String tag, String value) {
        return of(namespace(tag), name(tag), value);
    }

    public static Tag of(String namespace, @NotNull String name, String value) {
        return new Tag(namespace, name, value);
    }

    /**
     * Return true if the tag has a format otherwise false.
     *
     * @return flag indicating if the tag has a format field
     */
    public boolean hasValue() {
        return value != null;
    }

    public String text() {
        return (!Strings.isNullOrBlank(namespace()) ? "%s:".formatted(namespace()) : "") + name() + (value() != null ? "=%s".formatted(value()) : "");
    }
}
