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

/**
 * Defines a mixin and abstracts an entity with comments.
 */
public interface HasMutableComments extends HasComments {
    /**
     * Set the new collection of comments.
     *
     * @param comments new collection of comments
     * @see #comments()
     */
    void comments(Collection<Comment> comments);

    /**
     * Adds a comment to the list of comments.
     *
     * @param comment comment to be added, cannot be null
     */
    void add(@NotNull Comment comment);

    /**
     * Remove the given comment from the list of comments.
     *
     * @param comment comment to be removed, cannot be null
     */
    void remove(@NotNull Comment comment);

    /**
     * Add a list of comments.
     *
     * @param comments comments to be added, cannot be null
     * @see HasMutableComments#add(Comment)
     */
    default void addComments(@NotNull Iterable<Comment> comments) {
        comments.forEach(this::add);
    }
}
