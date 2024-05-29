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

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The interface defines a mixin and abstracts an entity with readable tags.
 */
public interface HasTags {
    /**
     * Return the collection of tags for the entity.
     *
     * @return collection of tags
     */
    Collection<Tag> tags();

    /**
     * Find the tag with the given tag identification containing optional namespace and tag name.
     *
     * @param tag tag identification of the tag to be removed
     * @return requested tag as optional
     */
    default Optional<Tag> findBy(@NotNull String tag) {
        return findBy(Tag.namespace(tag), Tag.name(tag));
    }

    /**
     * Search for a tag with the given namespace and name.
     *
     * @param namespace optional namespace of the tag to be found
     * @param name      name of the tag to be found
     * @return optional found tag
     */
    default Optional<Tag> findBy(String namespace, @NotNull String name) {
        Objects.requireNonNull(name);
        return tags().stream().filter(o -> Objects.equals(namespace, o.namespace()) && name.equals(o.name())).findAny();
    }

    default boolean containsTag(@NotNull String tag) {
        return findBy(tag).isPresent();
    }

    /**
     * True, if the tag with the given tag identification containing optional namespace and tag name could be found.
     *
     * @param namespace optional namespace of the tag
     * @param name      name of the tag
     * @return flag indicating if the tag is existing
     */
    default boolean containsTag(String namespace, @NotNull String name) {
        return findBy(namespace, name).isPresent();
    }

    /**
     * Return the value of the tag with the given qualified tag name.
     *
     * @param tag qualified tag name
     * @return the tag value if found
     */
    default Optional<String> value(@NotNull String tag) {
        return findBy(tag).map(Tag::value);
    }

    default Collection<Tag> findByNamespace(String namespace) {
        Objects.requireNonNull(namespace);
        return tags().stream().filter(o -> Objects.equals(namespace, o.namespace())).collect(Collectors.toSet());
    }

    /**
     * Return the collection of tags as a canonical string representation.
     *
     * @return text representation of the tag collection
     */
    default String rawTags() {
        return Tag.text(tags());
    }
}
