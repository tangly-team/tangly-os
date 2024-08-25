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

package net.tangly.core.domain;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The generator is responsible for creating a document from an entity and a set of properties. The properties are used to configure the generation process.
 * The configuration can also be defined in the constructor of the generator.
 * <p>The generator instance can be discarded after the generation of the document.</p>
 *
 * @param <T> type of the entity used to create the document
 */
public interface DocumentGenerator<T> {
    String ORGANIZATION_KEY = "tenant.organization.language";
    String COPYRIGHT_KEY = "tenant.organization.copyright";
    String THEME_PATH_KEY = "tenant.root.docs.directory";
    String LOGO = "logo";
    String THEME = "theme";

    /**
     * Creates a new output document.
     *
     * @param entity entity used to create a new invoice document
     * @param audit  domain audit sink to log the operation events
     */
    void export(@NotNull T entity, boolean overwrite, @NonNull Path document, @NotNull DomainAudit audit);

    default boolean shouldExport(@NonNull Path document, boolean overwrite) {
        return overwrite || !Files.exists(document);
    }
}
