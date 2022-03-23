/*
 * Copyright 2021-2022 Marcel Baumann
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
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class ExternalEntityImp implements ExternalEntity {
    private String id;
    private String name;
    private LocalDate date;
    private String text;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    protected ExternalEntityImp(@NotNull String id) {
        this();
        this.id = id;
    }

    protected ExternalEntityImp() {
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

    public void date(LocalDate date) {
        this.date = date;
    }

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
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ExternalEntityImp o) && Objects.equals(id(), o.id()) && Objects.equals(date(), o.date()) && Objects.equals(text(), o.text()) &&
            Objects.equals(comments(), o.comments()) && Objects.equals(tags(), o.tags());
    }
}
