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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * The interface defines a mixin and abstracts an entity with tags.
 */
public interface HasTags {
    /**
     * Returns the set of tags of the entity.
     *
     * @return set of tags
     */
    Set<Tag> tags();

    /**
     * Adds the tag to the set of tags.
     *
     * @param tag tag to be added
     * @return {@code true} if this set did not already contain the specified element
     */
    boolean add(Tag tag);

    default void addTags(@NotNull Iterable<Tag> tags) {
        tags.forEach(this::add);
    }

    void clearTags();

    /**
     * Replaces or inserts the given tag. Tag equivalence is detected with optional namespace and tag name.
     *
     * @param tag tag to replace or insert
     */
    default void update(@NotNull Tag tag) {
        Objects.requireNonNull(tag);
        findBy(tag.namespace(), tag.name()).ifPresent(this::remove);
        add(tag);
    }

    /**
     * Replaces or inserts the given tag. Tag equivalence is detected with optional namespace and tag name.
     *
     * @param tag   tag to replace or insert
     * @param value optional value of the tag
     */
    default void update(@NotNull String tag, String value) {
        Objects.requireNonNull(tag);
        update(Tag.of(tag, value));
    }

    /**
     * Removes the tag from the set of tags.
     *
     * @param tag tag to remove
     * @return {@code true} if this set contained the specified element
     */
    boolean remove(Tag tag);

    /**
     * Removes the tag with the given tag identification containing optional namespace and tag name.
     *
     * @param tag tag identification of the tag to be removed
     */
    default void removeTagNamed(@NotNull String tag) {
        findBy(tag).ifPresent(this::remove);
    }

    /**
     * Finds the tag with the given tag identification containing optional namespace and tag name.
     *
     * @param tag tag identification of the tag to be removed
     * @return requested tag as optional
     */
    default Optional<Tag> findBy(@NotNull String tag) {
        return findBy(Tag.namespace(tag), Tag.name(tag));
    }

    /**
     * Searches for a tag with the given namespace and name.
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
     * True if the tag with the given tag identification containing optional namespace and tag name could be found.
     *
     * @param namespace optional namespace of the tag
     * @param name      name of the tag
     * @return flag indicating if the tag is existing
     */
    default boolean containsTag(String namespace, @NotNull String name) {
        return findBy(namespace, name).isPresent();
    }

    /**
     * Returns the value of the tag with the given qualified tag name.
     *
     * @param tag qualified tag name
     * @return the tag value if found
     */
    default Optional<String> value(@NotNull String tag) {
        return findBy(tag).map(Tag::value);
    }

    default Set<Tag> findByNamespace(String namespace) {
        Objects.requireNonNull(namespace);
        return tags().stream().filter(o -> Objects.equals(namespace, o.namespace())).collect(Collectors.toSet());
    }

    /**
     * Returns the set of tags as a canonical string representation.
     *
     * @return text representation of the tag set
     * @see HasTags#rawTags(String)
     */
    default String rawTags() {
        return Tag.text(tags());
    }

    /**
     * Sets the tags using the canonical string representation.
     *
     * @param rawTags canonical representation of the tag set
     * @see HasTags#rawTags()
     */
    default void rawTags(String rawTags) {
        Tag.toTags(rawTags).forEach(this::update);
    }

    static <T extends HasTags> Collection<T> collect(@NotNull Collection<T> items, @NotNull String tag) {
        return items.stream().filter(o -> o.containsTag(tag)).toList();
    }
}
