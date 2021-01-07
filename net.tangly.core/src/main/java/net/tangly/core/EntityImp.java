/*
 * Copyright 2006-2021 Marcel Baumann
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of the Entity interface.
 *
 * @see QualifiedEntity
 */
public abstract class EntityImp implements Entity {
    private final long oid;
    private String text;
    private LocalDate fromDate;
    private LocalDate toDate;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    /**
     * Default constructor.
     */
    public EntityImp() {
        this.oid = UNDEFINED_OID;
        comments = new ArrayList<>();
        tags = new HashSet<>();
    }

    // region HasOid

    public long oid() {
        return oid;
    }

    // endregion

    // region Entity

    @Override
    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    @Override
    public LocalDate fromDate() {
        return fromDate;
    }

    @Override
    public void fromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    @Override
    public LocalDate toDate() {
        return toDate;
    }

    @Override
    public void toDate(LocalDate toDate) {
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
    public void add(Tag tag) {
        tags.add(tag);
    }

    @Override
    public void remove(Tag tag) {
        tags.remove(tag);
    }

    @Override
    public void clearTags() {
        tags.clear();
    }
    // endregion
}
