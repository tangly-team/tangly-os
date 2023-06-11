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

import java.time.LocalDate;
import java.util.*;

/**
 * Default implementation of the Entity interface.
 */
public abstract class EntityImp implements Entity {
    private final long oid;
    private String id;
    private String name;
    private LocalDate from;
    private LocalDate to;
    private String text;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    public static <T extends Entity> T init(T entity, String id, String name, LocalDate from, LocalDate to, String text) {
        entity.id(id);
        entity.name(name);
        entity.from(from);
        entity.to(to);
        entity.text(text);
        return entity;
    }

    protected EntityImp() {
        this(UNDEFINED_OID);
    }

    protected EntityImp(long oid) {
        this.oid = oid;
        comments = new ArrayList<>();
        tags = new HashSet<>();
    }

    protected EntityImp(long oid, String id, String name, LocalDate from, LocalDate to, String text) {
        this(oid);
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
        this.text = text;
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
    public LocalDate from() {
        return from;
    }

    @Override
    public void from(LocalDate fromDate) {
        this.from = fromDate;
    }

    @Override
    public LocalDate to() {
        return to;
    }

    @Override
    public void to(LocalDate toDate) {
        this.to = toDate;
    }

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
    public void removeAllTags() {
        tags.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(oid);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EntityImp o) && Objects.equals(oid(), o.oid()) && Objects.equals(from(), o.from()) && Objects.equals(to(), o.to()) &&
            Objects.equals(text(), o.text()) && Objects.equals(comments(), o.comments()) && Objects.equals(tags(), o.tags());
    }
}
