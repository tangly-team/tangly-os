/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.spec.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import net.tangly.spec.bdd.engine.StoryAsciiDocPublisher;
import net.tangly.spec.bdd.engine.StoryMerger;
import net.tangly.spec.bdd.engine.StoryWriter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Store definitions for unit tests.
 */
public class StoreUtilities {
    private static final String BDD_REPORT = "bdd-features.json";

    public static void main(String[] params) throws IOException {
        mergeStories();
    }

    @AfterAll
    public static void mergeStories() throws IOException {
        Path bddReportsFolder = StoryWriter.getOrCreateBddReportsFolder(StoreUtilities.class);
        Path bddReports = bddReportsFolder.resolve(Path.of(BDD_REPORT));
        Files.deleteIfExists(bddReports);

        StoryMerger merger = new StoryMerger();
        merger.merge(bddReportsFolder);
        merger.write(bddReports);

        validateSchema(bddReports);

        Path bddPublished = bddReportsFolder.resolve(Path.of("bdd-report.adoc"));
        new StoryAsciiDocPublisher(bddReports, bddPublished);
    }

    public static Store create(@NotNull Scene scene, int blackSweaters, int blueSweaters) {
        Store store = new Store(blackSweaters, blueSweaters);
        scene.put("store", store);
        return store;
    }

    public static Store store(@NotNull Scene scene) {
        return (Store) scene.get("store");
    }

    /**
     * Validate the behavior driven aggregate test report against the JSON schema describing the structure of the report.
     *
     * @param bddReport report which structure should be validated
     * @throws IOException if a file could not be found or opened
     */
    private static void validateSchema(Path bddReport) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bdd-schema.json");
             InputStream stream = new BufferedInputStream(Files.newInputStream(bddReport))) {
            JsonSchema schema = factory.getSchema(is);
            JsonNode node = mapper.readTree(stream);
            assertThat(schema.validate(node)).isEmpty();
        }
    }
}
