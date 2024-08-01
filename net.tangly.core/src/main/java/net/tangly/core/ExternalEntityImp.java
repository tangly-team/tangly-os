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

public abstract class ExternalEntityImp implements ExternalEntity {
    private final String id;
    private final String name;
    private final LocalDate date;
    private final String text;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    protected ExternalEntityImp(@NotNull String id, String name, LocalDate date, String text) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.text = text;
        comments = new ArrayList<>();
        tags = new HashSet<>();
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
    public String text() {
        return text;
    }

    @Override
    public LocalDate date() {
        return date;
    }


    // region Comments

    @Override
    public Collection<Comment> comments() {
        return Collections.unmodifiableList(comments);
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
