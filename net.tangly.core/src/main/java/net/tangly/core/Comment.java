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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * Defines a human-readable annotation to an entity. A comment is an immutable object. Comments can be tagged to provide classification. A comment belongs to
 * the entity owning it an is only accessible through this entity.
 *
 * @param created timestamp when the comment was created
 * @param author  of the comment instance as human-readable field
 * @param text    text of the comment, we recommend using asciidoc format
 * @param tags    set of tags associated with the comment
 */
public record Comment(LocalDateTime created, String author, String text, Set<Tag> tags) implements HasTags {

    /**
     * Default constructor to create an immutable instance.
     *
     * @param created timestamp when the comment was created
     * @param author  of the comment instance as human-readable field
     * @param text    text of the comment, we recommend using asciidoc format
     */
    public Comment(@NotNull LocalDateTime created, @NotNull String author, @NotNull String text) {
        this(created, author, text, Collections.emptySet());
    }

    public static Comment of(@NotNull String author, @NotNull String text) {
        return new Comment(LocalDateTime.now(), author, text);
    }
}
