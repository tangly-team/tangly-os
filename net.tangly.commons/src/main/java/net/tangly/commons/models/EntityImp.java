/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.commons.models;

import java.time.LocalDate;
import java.util.*;

/**
 * Default implementation of the Entity interface.
 *
 * @see Entity
 */
public class EntityImp implements Entity {
    private final long oid;
    private String id;
    private String name;
    private String text;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<Comment> comments;
    private Set<Tag> tags;

    /**
     * Default constructor.
     *
     * @param oid unique internal object identifier
     * @param id  external object identifier
     */
    public EntityImp(long oid, String id, String name) {
        this.oid = oid;
        this.id = id;
        this.name = name;
        comments = new ArrayList<>();
        tags = new HashSet<>();
    }

    // region HasOid
    @Override
    public long oid() {
        return oid;
    }

    // endregion
    // region HasId
    @Override
    public String id() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    @Override
    public String name() {
        return name;
    }
    // endregion
    // region Entity

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
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public void add(Comment comment) {
        comment.foid(oid);
        comments.add(comment);
    }

    @Override
    public void remove(Comment comment) {
        comments.remove(comment);
        comment.foid(HasId.UNDEFINED_OID);
    }
    // endregion
    // region Tags

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
    public void clear() {
        tags.clear();
    }
    // endregion
}
