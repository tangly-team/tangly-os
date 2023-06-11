/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Defines a human-readable annotation to an entity. A comment is an immutable object. Comments can be tagged to provide classification. A comment belongs to
 * the entity owning it an is only accessible through this entity.
 */
public class Comment implements HasTags {
    /**
     * The creation date and time of the comment.
     */
    private LocalDateTime created;

    /**
     * The author of the comment, the system should insure that the author is a unique external identifier.
     */
    private final String author;

    /**
     * The comment as a Markdown text.
     */
    private final String text;

    /**
     * The tags of the comment.
     */
    private final Set<Tag> tags;


    /**
     * Default constructor to create an immutable instance.
     *
     * @param author of the comment instance as human-readable field
     * @param text   text of the comment, we recommend using asciidoc format
     */
    public Comment(@NotNull String author, @NotNull String text) {
        this.created = LocalDateTime.now();
        this.author = Objects.requireNonNull(author);
        this.text = Objects.requireNonNull(text);
        tags = new HashSet<>();
    }

    /**
     * Factory method to update a new comment. The current date and time are set a creation date.
     *
     * @param author author of the comment
     * @param text   content of the comment
     * @param tags   optional tags of the comment
     * @return the newly created comment
     */
    @SafeVarargs
    public static Comment of(@NotNull String author, @NotNull String text, Tag... tags) {
        Comment comment = new Comment(author, text);
        comment.addTags(Set.of(tags));
        return comment;
    }

    /**
     * Factory method to update a new comment. The current date and time are set a creation date.
     *
     * @param created creation date of the comment
     * @param author  author of the comment
     * @param text    content of the comment
     * @return the newly created comment
     */
    public static Comment of(@NotNull LocalDateTime created, @NotNull String author, @NotNull String text) {
        var comment = new Comment(author, text);
        comment.created = created;
        return comment;
    }

    /**
     * Factory method to update a new comment. The current date and time are set a creation date.
     *
     * @param created creation date of the comment
     * @param author  author of the comment
     * @param text    content of the comment
     * @param tags    optional tags of the comment
     * @return the newly created comment
     */
    @SafeVarargs
    public static Comment of(@NotNull LocalDateTime created, @NotNull String author, @NotNull String text, Tag... tags) {
        var comment = new Comment(author, text);
        comment.created = created;
        comment.addTags(Set.of(tags));
        return comment;
    }

    /**
     * Returns the timestamp when the comment was created.
     *
     * @return timestamp of the creation of the comment
     */
    public @NotNull LocalDateTime created() {
        return created;
    }

    /**
     * Returns the author of the comment as human-readable information.
     *
     * @return author of the comment
     */
    public @NotNull String author() {
        return author;
    }

    /**
     * Returns the text of the comment. Asciidoc is a preferred format
     *
     * @return text of the comment
     */
    public @NotNull String text() {
        return text;
    }

    // endregion

    // region HasTags

    @Override
    public @NotNull Set<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public boolean add(@NotNull Tag tag) {
        return tags.add(tag);
    }

    @Override
    public boolean remove(@NotNull Tag tag) {
        return tags.remove(tag);
    }

    @Override
    public void removeAllTags() {
        tags.clear();
    }

    // endregion
    @Override
    public int hashCode() {
        return Objects.hash(created, author, text, tags);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Comment o) && Objects.equals(created(), o.created()) && Objects.equals(author(), o.author()) &&
            Objects.equals(text(), o.text()) && Objects.equals(tags(), o.tags());
    }

    @Override
    public String toString() {
        return String.format("created=%s, author=%s, text=%s, tags=%s", created(), author(), text(), Tag.text(tags));
    }
}
