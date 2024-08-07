/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.core.documents;

import net.tangly.core.DateRange;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.*;

public class Documents {
    private final List<Document> documents;
    private final String domain;
    private final URI documentsRoot;

    public Documents(@NotNull Collection<Document> documents, @NotNull String domain, @NotNull URI documentsRoot) {
        this.documents = new ArrayList<>(documents);
        this.domain = domain;
        this.documentsRoot = documentsRoot;
    }

    public List<Document> findBy(@NotNull String name) {
        return Collections.emptyList();
    }

    public List<Document> findBy(@NotNull DateRange range) {
        return Collections.emptyList();
    }

    public List<Document> findBy(Map<String, String> values) {
        return Collections.emptyList();
    }

    public void add(@NotNull Document document) {
        documents.add(document);
    }

    public List<Document> documents() {
        return Collections.unmodifiableList(documents);
    }
}
