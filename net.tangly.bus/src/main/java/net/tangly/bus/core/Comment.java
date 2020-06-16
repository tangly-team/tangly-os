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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * Comment defines a human-readable annotation to an entity. A comment is an immutable object. Comments can be tagged to provide classification.
 */
public class Comment implements HasOid, HasTags {
    private static final long serialVersionUID = 1L;

    /**
     * Unique object identifier of the comment instance.
     */
    private long oid;

    /**
     * Foreign identifier of the entity owning the comment.
     */
    private long ownedBy;

    /**
     * The creation date and time of the comment.
     */
    private LocalDateTime created;

    /**
     * The author of the comment, the system should insures that the author is an unique external identifier.
     */
    private String author;

    /**
     * The comment as a markdown text.
     */
    private String text;

    /**
     * The tags of the comment.
     */
    private Set<Tag> tags;

    /**
     * Private constructor to support JSON serialization.
     */
    public Comment() {
    }

    /**
     * Default constructor to create an immutable instance.
     *
     * @param timestamp when the comment instance was created
     * @param author    of the comment instance as human readable field
     * @param text      text of the comment, we recommend using asciidoc format
     */
    public Comment(@NotNull LocalDateTime timestamp, @NotNull String author, @NotNull String text) {
        this.oid = HasOid.UNDEFINED_OID;
        this.created = Objects.requireNonNull(timestamp);
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
    public static Comment of(@NotNull String author, @NotNull String text, Tag... tags) {
        return of(LocalDateTime.now(), author, text, tags);
    }

    /**
     * Factory method to update a new comment. The current date and time are set a creation date.
     *
     * @param date   creation date of the comment
     * @param author author of the comment
     * @param text   content of the comment
     * @param tags   optional tags of the comment
     * @return the newly created comment
     */
    public static Comment of(@NotNull LocalDateTime date, @NotNull String author, @NotNull String text, Tag... tags) {
        var comment = new Comment(date, author, text);
        Arrays.stream(tags).forEach(comment::add);
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

    // region HasOid

    @Override
    public long oid() {
        return oid;
    }

    // endregion

    // region HasTags

    @Override
    public @NotNull Set<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void add(@NotNull Tag tag) {
        tags.add(tag);
    }

    @Override
    public void remove(@NotNull Tag tag) {
        tags.remove(tag);
    }

    @Override
    public void clearTags() {
        tags.clear();
    }
    // endregion

    @Override
    public String toString() {
        return String.format("created=%1$%tF-%1$%tT, author=%2$s, text=%3$s, tags=%4$s", created, author, text, Tag.text(tags));
    }
}
