/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to process and validate JSON files.
 */
public class JsonUtilities {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtilities.class);

    /**
     * Check if the JSON file is valid against the JSON schema describing the structure of the JSON file.
     *
     * @param jsonFile JSON file which structure should be validated
     * @param resource name of the resource containing the JSON schema to validate against
     * @return true if no validation error was found otherwise false
     */
    public static boolean isValid(@NotNull Path jsonFile, @NotNull String resource) {
        Set<ValidationMessage> messages = validateSchema(jsonFile, resource);
        messages.forEach(o -> logger.atDebug().log("JSON validation error: {}", o));
        return validateSchema(jsonFile, resource).isEmpty();
    }

    /**
     * Validate the JSON file against the JSON schema describing the structure of the JSON file.
     *
     * @param jsonFile JSON file which structure should be validated
     * @param resource name of the resource containing the JSON schema to validate against
     * @return set of validation messages if validation errors were found otherwise empty set
     */
    public static Set<ValidationMessage> validateSchema(@NotNull Path jsonFile, @NotNull String resource) {
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(jsonFile))) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(stream);

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            JsonSchema schema = factory.getSchema(is);
            return schema.validate(node);
        } catch (IOException e) {
            logger.atError().setCause(e).log("IO Exception when processing {}", jsonFile);
            throw new UncheckedIOException(e);
        }
    }
}
