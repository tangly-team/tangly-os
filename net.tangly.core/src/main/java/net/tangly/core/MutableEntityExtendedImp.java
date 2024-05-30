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

import java.time.LocalDate;
import java.util.*;

/**
 * Default implementation of the {@link MutableEntityExtended} interface. The unique object identifier shall be set at construction.
 */
public abstract class MutableEntityExtendedImp implements MutableEntityExtended {
    private final long oid;
    private String id;
    private String name;
    private DateRange dateRange;
    private String text;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    public static <T extends MutableEntityExtended> T init(T entity, String id, String name, LocalDate from, LocalDate to, String text) {
        entity.id(id);
        entity.name(name);
        entity.range(DateRange.of(from, to));
        entity.text(text);
        return entity;
    }

    protected MutableEntityExtendedImp(long oid) {
        this.oid = oid;
        dateRange = DateRange.INFINITE;
        comments = new ArrayList<>();
        tags = new HashSet<>();
    }

    @Override
    public long oid() {
        return oid;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void id(String id) {
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
    public DateRange range() {
        return dateRange;
    }

    @Override
    public void range(@NotNull DateRange range) {
        this.dateRange = Objects.nonNull(range) ? range : DateRange.INFINITE;
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

    @Override
    public Collection<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void tags(@NotNull Collection<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
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
    public void clear() {
        tags.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(oid);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MutableEntityExtendedImp o) && Objects.equals(oid(), o.oid()) && Objects.equals(range(), o.range()) && Objects.equals(text(), o.text()) &&
            Objects.equals(comments(), o.comments()) && Objects.equals(tags(), o.tags());
    }
}
