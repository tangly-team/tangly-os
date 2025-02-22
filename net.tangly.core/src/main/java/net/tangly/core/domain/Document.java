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

import net.tangly.commons.logger.EventData;
import net.tangly.core.*;
import net.tangly.core.events.EntityChangedInternalEvent;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * Defines a document and its metadata stored in the system as immutable record.
 * A document is owed by a domain and is part of the domain model.
 * The domain handles the access rights to the document and the retention policies.
 *
 * @param id        uri is relative to the domain documents root folder. It is unique within the domain.
 * @param name      human-readable name of the document
 * @param time      time when the document was created
 * @param range     date range of the information contained in the document. If the document is a snapshot of a state, the range is the date of the snapshot
 * @param text      human-readable AsciiDoc text describing the document
 * @param generated flag indicating if the document was generated by the ERP system. A generated document is not editable and is not subject to retention policies.
 *                  It can be regenerated at any time.
 * @param tags      collection of tags associated with the document
 */
public record Document(@NotNull String id, @NotNull String name, String extension, @NotNull LocalDateTime time, @NotNull DateRange range, String text,
                       boolean generated, @NotNull Collection<Tag> tags) implements HasId, HasName, HasDateRange, HasText, HasTags {
    public static void update(@NotNull Provider<Document> provider, @NotNull Document document, @NotNull DomainAudit audit) {
        Provider.findById(provider, document.id()).ifPresentOrElse(o -> {
            provider.delete(o);
            provider.update(document);
            audit.log(EventData.DOCUMENT_EVENT, EventData.Status.INFO, "Document was superseded.", Map.of("old-document", o, "superseding-document", document));
            audit.submitInterally(new EntityChangedInternalEvent(audit.name(), Document.class.getSimpleName(), Operation.REPLACE));
        }, () -> {
            provider.update(document);
            audit.log(EventData.DOCUMENT_EVENT, EventData.Status.INFO, "Document was created.", Map.of("document", document));
            audit.submitInterally(new EntityChangedInternalEvent(audit.name(), Document.class.getSimpleName(), Operation.CREATE));
        });
    }
}
