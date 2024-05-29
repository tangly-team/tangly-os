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

/**
 * The interface defines a mixin and abstracts an entity with tags.
 */
public interface HasMutableTags extends HasTags {
    /**
     * Set the new collection of tags for the entity.
     *
     * @param tags new collection of tags
     * @see #tags()
     */
    void tags(@NotNull Collection<Tag> tags);

    /**
     * Remove the tag from the collection of tags.
     *
     * @param tag tag to remove
     * @return {@code true} if this collection contained the specified element
     * @see #add(Tag)
     */
    boolean remove(@NotNull Tag tag);

    /**
     * Add the tag to the collection of tags.
     *
     * @param tag tag to be added
     * @return {@code true} if this collection did not already contain the specified element
     * @see #remove(Tag)
     */
    boolean add(@NotNull Tag tag);

    void clear();

    default void addTags(@NotNull Iterable<Tag> tags) {
        tags.forEach(this::add);
    }

    /**
     * Replace or insert the given tag. Tag equivalence is detected with optional namespace and tag name.
     *
     * @param tag tag to replace or insert
     */
    default boolean update(@NotNull Tag tag) {
        Objects.requireNonNull(tag);
        findBy(tag.namespace(), tag.name()).ifPresent(this::remove);
        return add(tag);
    }

    /**
     * Replace or inserts the given tag. Tag equivalence is detected with optional namespace and tag name.
     *
     * @param tag   tag to replace or insert
     * @param value optional value of the tag
     */
    default void update(@NotNull String tag, String value) {
        Objects.requireNonNull(tag);
        update(Tag.of(tag, value));
    }

    /**
     * Remove the tag with the given tag identification containing optional namespace and tag name.
     *
     * @param tag tag identification of the tag to be removed
     */
    default void removeTagNamed(@NotNull String tag) {
        findBy(tag).ifPresent(this::remove);
    }

    /**
     * Set the tags using the canonical string representation.
     *
     * @param rawTags canonical representation of the tag collection
     * @see HasMutableTags#rawTags()
     */
    default void rawTags(String rawTags) {
        Tag.toTags(rawTags).forEach(this::update);
    }
}
