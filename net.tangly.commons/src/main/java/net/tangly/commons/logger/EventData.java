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

package net.tangly.commons.logger;

import org.apache.logging.log4j.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Provide a simple approach to create an audit trail for all relevant operations performed in the system. Contextual information such as user, IP address are
 * part of the MDR context and implicitly available to the event data logging process.
 * <p>Good practice is to log all incoming and outgoing data. The granularity should be an aggregate as defined in the domain-driven design approach.
 * The goal is provide an audit trail for all relevant input and output operations performed in the system.</p>
 * <p></p>Statuses are:</p>
 * <dl>
 *     <dt>Success</dt> <dd>A complex operation was completed successfully. For example all entities of a TSV file could be imported.</dd>
 *     <dt>Info</dt> <dd>Status information of a completed system activity. For example one entity of a TSV file could be imported.</dd>
 *     <dt>Warning</dt> <dd>The operation encountered a problem but the data could be processed.</dd>
 *     <dt>Error</dt> <dd>The data could not be processed due to syntactic or semantic errors.</dd>
 *     <dt>Failure</dt> <dd>The operation failed due to an internal or resource problem. For example the file could be found or read.</dd>
 * </dl>
 *
 * @param event     event triggering the creation of an audit log
 * @param timestamp timestamp when the audit log was created
 * @param domain    domain source of the audit log.
 * @param status    status associated with the audit log
 * @param text      text of the audit log. It should be English and provide a concise description of the event
 * @param data      data specific to the audit log
 * @param exception optional exception to be added to the audit log
 */
public record EventData(@NotNull String event, @NotNull LocalDateTime timestamp, @NotNull String domain, @NotNull Status status, String text,
                        @NotNull Map<String, Object> data, Throwable exception) {
    public static final String IMPORT_EVENT = "import";
    public static final String EXPORT_EVENT = "export";
    public static final String CLEAR_EVENT = "clear";
    public static final String DOCUMENT_EVENT = "document";
    public static final String REPORT_EVENT = "report";
    public static final String FILENAME = "filename";
    public static final String ENTITY = "entity";

    public enum Status {SUCCESS, INFO, WARNING, ERROR, FAILURE}

    private static final String AUDIT_LOGGER = "AuditLogger";
    private static final Marker MARKER = MarkerManager.getMarker("AUDIT_EVENT");

    public static EventData of(@NotNull String event, @NotNull String domain, @NotNull Status status, String reason, @NotNull Map<String, Object> data,
                               Throwable exception) {
        return new EventData(event, LocalDateTime.now(), domain, status, reason, data, exception);
    }

    /**
     * Log the event data as an audit log.
     *
     * @param data event data to add to the audit trail
     */
    public static void log(@NotNull EventData data) {
        final Logger logger = LogManager.getLogger(AUDIT_LOGGER);
        LogBuilder builder = logger.atInfo().withMarker(MARKER);
        if (Objects.nonNull(data.exception())) {
            builder.withThrowable(data.exception());
        }
        if (data.exception() != null) {
            builder.log("{}-{}-{}-{}:{}:{} - {} ", data.event(), data.timestamp(), data.domain(), data.status(), data.text(), data.data(), data.exception());
        } else {
            builder.log("{}-{}-{}-{}:{}:{}", data.event(), data.timestamp(), data.domain(), data.status(), data.text(), data.data());
        }
    }
}
