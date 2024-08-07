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

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Defines a document and its metadata stored in the system.
 * @param name       human-readable name of the document
 * @param domain     domain name to which the document belongs to
 * @param uri       uri is relative to the domain documents root folder
 * @param text       human-readable text describing the document
 * @param commercial flag indicating if the document is of commercial relevance. Commercial documents are subject to retention policies.
 * @param values     map of key-value pairs used to store metadata of the document
 */
public record Document(@NotNull String name, @NotNull String domain, @NotNull String uri, String text, boolean commercial,
                       @NotNull Map<String, String> values) {
    static final String AUTHOR = "author";
    static final String CREATION_DATE = "creationDate";
    static final String ID = "id";
    static final String MEDIA_TYPE = "mediaType";
    static final String READONLY = "readonly";
}
