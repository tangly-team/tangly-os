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

package net.tangly.commons.logger;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

/**
 * Provide a simple approach to create an audit trail for all relevant operations performed in the system. Contextual information such as user, IP address are
 * part of the MDR context and implicitly available to the event data logging process.
 *
 * @param event     event triggering the creation of an audit log
 * @param timestamp timestanmp when the audit log was created
 * @param component component source of the audit log. By convention we use the module name or if necessary the package name
 * @param status    status associated with the audit log
 * @param text      text of the audit log
 * @param data      data specific to the audit log
 * @param exception optional exception to be added to the audit log
 */
public record EventData(@NotNull String event, @NotNull LocalDateTime timestamp, @NotNull String component, @NotNull Status status, String text,
                        @NotNull Map<String, Object> data, Throwable exception) {
    public static final String IMPORT = "import";
    public static final String EXPORT = "export";

    public enum Status {SUCCESS, INFO, WARNING, FAILURE}

    private static final String AUDIT_LOGGER = "AuditLogger";
    private static final Marker MARKER = MarkerManager.getMarker("AUDIT_EVENT");

    public static void log(@NotNull String event, @NotNull String component, @NotNull Status status, String reason, @NotNull Map<String, Object> data) {
        log(new EventData(event, LocalDateTime.now(), component, status, reason, data, null));
    }

    public static void log(@NotNull String event, @NotNull String component, @NotNull Status status, String reason, @NotNull Map<String, Object> data,
                           Throwable exception) {
        log(new EventData(event, LocalDateTime.now(), component, status, reason, data, exception));
    }

    /**
     * Log the event data as audit log.
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
            builder.log("{}-{}-{}-{}:{}:{} - {} ", data.event(), data.timestamp(), data.component(), data.status(), data.text(), data.data(), data.exception());
        } else {
            builder.log("{}-{}-{}-{}:{}:{}", data.event(), data.timestamp(), data.component(), data.status(), data.text(), data.data());
        }
    }
}
