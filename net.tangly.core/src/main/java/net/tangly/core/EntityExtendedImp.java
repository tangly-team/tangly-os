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

import java.time.LocalDate;
import java.util.*;

/**
 * Default implementation of the {@link EntityExtended} interface. The unique object identifier shall be set at construction.
 */
public abstract class EntityExtendedImp implements EntityExtended {
    private final long oid;
    private String id;
    private String name;
    private final DateRange dateRange;
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

    protected EntityExtendedImp(long oid) {
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
    public String name() {
        return name;
    }

    @Override
    public DateRange range() {
        return dateRange;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public Collection<Comment> comments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public Collection<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(oid);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EntityExtendedImp o) && Objects.equals(oid(), o.oid()) && Objects.equals(range(), o.range()) && Objects.equals(text(),
            o.text()) && Objects.equals(comments(), o.comments()) && Objects.equals(tags(), o.tags());
    }
}
