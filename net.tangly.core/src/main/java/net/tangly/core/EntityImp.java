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

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;

/**
 * Default implementation of the Entity interface.
 *
 * @see QualifiedEntity
 */
public abstract class EntityImp implements Entity {
    private final long oid;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String name;
    private String text;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    protected EntityImp() {
        this.oid = UNDEFINED_OID;
        comments = new ArrayList<>();
        tags = new HashSet<>();
    }

    // region HasOid

    @Override
    public long oid() {
        return oid;
    }

    // endregion

    // region HasText

    @Override
    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    // endregion

    // region HasInterval

    @Override
    public LocalDate from() {
        return fromDate;
    }

    @Override
    public void from(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    @Override
    public LocalDate to() {
        return toDate;
    }

    @Override
    public void to(LocalDate toDate) {
        this.toDate = toDate;
    }

    // endregion

    // region Comments

    @Override
    public List<Comment> comments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public void add(@NotNull Comment comment) {
        comments.add(comment);
    }

    @Override
    public void remove(@NotNull Comment comment) {
        comments.remove(comment);
    }
    // endregion

    // region HasTags

    @Override
    public Set<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public boolean add(Tag tag) {
        return tags.add(tag);
    }

    @Override
    public boolean remove(Tag tag) {
        return tags.remove(tag);
    }

    @Override
    public void clearTags() {
        tags.clear();
    }
    // endregion

    @Override
    public int hashCode() {
        return Objects.hashCode(oid);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EntityImp o) && Objects.equals(oid(), o.oid()) && Objects.equals(from(), o.from()) &&
            Objects.equals(to(), o.to()) && Objects.equals(text(), o.text()) && Objects.equals(comments(), o.comments()) &&
            Objects.equals(tags(), o.tags());
    }
}
