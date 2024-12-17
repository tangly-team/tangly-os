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

package net.tangly.dev.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositoryFile {
    private String name;
    private List<CommitItem> changes;

    public RepositoryFile() {
        changes = new ArrayList<>();
    }

    public RepositoryFile(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void add(CommitItem change) {
        changes.add(change);
    }

    public List<CommitItem> changes() {
        return Collections.unmodifiableList(changes);
    }
}
