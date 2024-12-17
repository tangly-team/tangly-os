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

package net.tangly.commons.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Utilities to process and validate JSON files.
 */
public final class ValidatorUtilities {
    private static final Logger logger = LogManager.getLogger();

    private ValidatorUtilities() {
    }

    /**
     * Check if the JSON file is valid against the schema describing the structure of the file.
     *
     * @param path   JSON file which structure should be validated
     * @param schema name of the schema containing the schema to validate against
     * @return true if no validation error was found otherwise false
     */
    public static boolean isJsonValid(@NotNull Path path, @NotNull String schema) {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return isJsonValid(reader, schema);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Check if the YAML file is valid against the schema describing the structure of the file.
     *
     * @param path   YAML file which structure should be validated
     * @param schema name of the schema containing the schema to validate against
     * @return true if no validation error was found otherwise false
     */
    public static boolean isYamValid(@NotNull Path path, @NotNull String schema) {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return isJsonValid(reader, schema);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isJsonValid(@NotNull Reader reader, @NotNull String schema) {
        Set<ValidationMessage> messages = validateSchema(reader, schema, new ObjectMapper());
        messages.forEach(o -> logger.atDebug().log("validation error: {}", o));
        return messages.isEmpty();
    }

    public static boolean isYamlValid(@NotNull Reader reader, @NotNull String schema) {
        Set<ValidationMessage> messages = validateSchema(reader, schema, new ObjectMapper(new YAMLFactory()));
        messages.forEach(o -> logger.atDebug().log("validation error: {}", o));
        return messages.isEmpty();
    }

    /**
     * Validate the JSON file against the JSON schema describing the structure of the JSON file.
     *
     * @param schema name of the schema containing the JSON schema to validate against
     * @return set of validation messages if validation errors were found otherwise empty set
     */
    private static Set<ValidationMessage> validateSchema(@NotNull Reader reader, @NotNull String schema, ObjectMapper mapper) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schema)) {
            JsonNode node = mapper.readTree(reader);
            return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(is).validate(node);
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("IO Exception when processing");
            throw new UncheckedIOException(e);
        }
    }
}
