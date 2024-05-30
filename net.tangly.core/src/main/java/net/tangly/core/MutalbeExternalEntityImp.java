/*
 * Copyright 2021-2024 Marcel Baumann
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

import java.time.LocalDate;
import java.util.*;

public class MutalbeExternalEntityImp implements MutableExternalEntity {
    private String id;
    private String name;
    private LocalDate date;
    private String text;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    protected MutalbeExternalEntityImp(@NotNull String id) {
        this.id = id;
        comments = new ArrayList<>();
        tags = new HashSet<>();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void id(@NotNull String id) {
        this.id = id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    @Override
    public LocalDate date() {
        return date;
    }

    @Override
    public void date(LocalDate date) {
        this.date = date;
    }

    // region Comments

    @Override
    public Collection<Comment> comments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public void comments(Collection<Comment> comments) {
        this.comments.clear();
        this.comments.addAll(comments);
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
    public Collection<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    public void tags(@NotNull Collection<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    @Override
    public void clear() {
        tags.clear();
    }

    @Override
    public boolean add(Tag tag) {
        return tags.add(tag);
    }

    @Override
    public boolean remove(Tag tag) {
        return tags.remove(tag);
    }

    // endregion

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MutalbeExternalEntityImp o) && Objects.equals(id(), o.id()) && Objects.equals(date(), o.date()) && Objects.equals(text(), o.text()) &&
            Objects.equals(comments(), o.comments()) && Objects.equals(tags(), o.tags());
    }
}
