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

package net.tangly.dev.model;


import net.tangly.commons.lang.Preconditions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commit {
    private final String id;
    private final Committer committer;
    private final LocalDate timestamp;
    private final String comment;
    private final List<CommitItem> items;

    public Commit(String id, Committer committer, LocalDate timestamp, String comment) {
        this.id = id;
        this.committer = committer;
        this.timestamp = timestamp;
        this.comment = comment;
        items = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public Committer committer() {
        return committer;
    }

    public LocalDate timestamp() {
        return timestamp;
    }

    public String comment() {
        return comment;
    }

    public void add(CommitItem item) {
        Preconditions.checkArgument(item.commit() == this);
        items.add(item);
    }

    public List<CommitItem> items() {
        return Collections.unmodifiableList(items);
    }
}
