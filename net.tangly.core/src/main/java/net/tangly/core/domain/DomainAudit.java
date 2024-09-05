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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Defines the interface to audit relevant events specific to a bounded domain.
 * Bounded domain should audit all imported and exported aggregates.
 * The fact that a report was generated should also be audited.
 * <p>The following rules are recommended:</p>
 * <ul>
 *     <li>SUCCESS if all items of a file could be read or written.</li>
 *     <li>WARNING if not all items of a file could be read or written.</li>
 *     <li>ERROR if an item could not be read or written.</li>
 *     <li>INFO if an item could be read or written.</li>
 *     <li>FAILURE if file could not be read or written.</li>
 * </ul>
 */
public interface DomainAudit {
    /**
     * Returns the name of the domain audit. It should be the name of the bounded domain.
     *
     * @return name of the domain audit
     */
    String name();

    void log(@NotNull EventData auditEvent);

    default void log(@NotNull String event, @NotNull EventData.Status status, String text, @NotNull Map<String, Object> data) {
        log(EventData.of(event, name(), status, text, data, null));
    }

    default void log(@NotNull String event, @NotNull EventData.Status status, String reason, @NotNull Map<String, Object> data, Throwable exception) {
        log(EventData.of(event, name(), status, reason, data, exception));
    }

    /**
     * Instances were added, deleted or modified in the domain programmatically and not through the user interface.
     *
     * @param entityName name of the entity
     */
    void entityImported(@NotNull String entityName);

    /**
     * Submit an event to the internal event channel of the domain. The channel is used to publish events within the domain.
     *
     * @param event event to submit
     */
    void submitInterally(@NotNull Object event);

    /**
     * Submit an event to the external public event channel of the domain. The channel is used to publish events to other domains.
     *
     * @param event event to submit
     */
    void submit(@NotNull Object event);

}
