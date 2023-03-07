/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.bdd.engine;

import net.tangly.bdd.Scene;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Writes the story with all executed scenarios in a JSON file. The feature associated with the package of the story class is also added to the
 * generated file.
 */
public class StoryWriter {
    private static final Set<String> KNOWN_TARGET_DIRS = Set.of("target", "build", "out");
    private final StoryRun run;
    private final Path path;

    public StoryWriter(@NotNull StoryRun run) {
        this.run = run;
        this.path = Paths.get(getOrCreateBddReportsFolder(run.getClass()).toString(), run.clazz().getName() + BddConstants.EXT);
    }

    public static Path getOrCreateBddReportsFolder(@NotNull Class<?> clazz) {
        URL url = clazz.getResource("");
        Objects.requireNonNull(url);
        var targetDir = Paths.get(url.getPath());
        int index = targetDir.getNameCount() - 1;
        while ((index > 0) && (!KNOWN_TARGET_DIRS.contains(targetDir.getName(index).toString()))) {
            --index;
            targetDir = targetDir.getParent();
        }
        Path bddReportsDirectory = targetDir.resolve(Paths.get(BddConstants.BDD_REPORTS_FOLDER));
        File bddReports = bddReportsDirectory.toFile();
        if (!bddReports.exists() && !bddReports.mkdir()) {
            throw new IllegalStateException("Unable to create the folder for saving bdd reports.");
        }
        return bddReportsDirectory;
    }

    void write() {
        // write feature
        var features = new JSONArray();
        JSONObject feature = createFeature(run);
        features.put(feature);

        // write story
        JSONObject story = createStory(run);
        feature.getJSONArray(BddConstants.STORIES).put(story);

        // write scenario
        run.scenes().forEach(scene -> story.getJSONArray(BddConstants.SCENARIOS).put(createScenario(scene)));

        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(features.toString(4));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JSONObject createFeature(StoryRun run) {
        var feature = new JSONObject();
        feature.put(BddConstants.PACKAGE_NAME, run.packages().getName());
        feature.put(BddConstants.NAME, run.featureName());
        feature.put(BddConstants.ID, run.featureId());
        feature.put(BddConstants.DESCRIPTION, run.featureDescription());
        createTags(run.featureTags(), feature);
        feature.put(BddConstants.STORIES, new JSONArray());
        return feature;
    }

    private static JSONObject createStory(StoryRun run) {
        var story = new JSONObject();
        story.put(BddConstants.CLASS_NAME, run.clazz().getName());
        story.put(BddConstants.NAME, run.name());
        story.put(BddConstants.ID, run.id());
        story.put(BddConstants.DESCRIPTION, run.description());
        createTags(run.storyTags(), story);
        story.put(BddConstants.SCENARIOS, new JSONArray());
        return story;
    }

    private static JSONObject createScenario(@NotNull Scene scene) {
        var scenario = new JSONObject();
        scenario.put(BddConstants.METHOD_NAME, scene.methodName());
        scenario.put(BddConstants.NAME, scene.description());
        if (!scene.given().text().isBlank()) {
            var given = new JSONObject();
            given.put(BddConstants.TEXT, scene.given().text());
            addAnds(scene.given().ands(), given);
            scenario.put(BddConstants.GIVEN, given);
        }
        if (!scene.when().text().isBlank()) {
            var when = new JSONObject();
            when.put(BddConstants.TEXT, scene.when().text());
            scenario.put(BddConstants.WHEN, when);
        }
        var then = new JSONObject();
        then.put(BddConstants.TEXT, scene.then().text());
        addAnds(scene.then().ands(), then);
        scenario.put(BddConstants.THEN, then);
        return scenario;
    }

    private static void addAnds(@NotNull List<String> ands, @NotNull JSONObject object) {
        addList(ands, object, BddConstants.AND);
    }

    private static void createTags(@NotNull List<String> tags, @NotNull JSONObject object) {
        addList(tags, object, BddConstants.TAGS);
    }

    private static void addList(@NotNull List<String> items, @NotNull JSONObject object, String jsonTag) {
        if (!items.isEmpty()) {
            var jsonArray = new JSONArray();
            items.forEach(jsonArray::put);
            object.put(jsonTag, jsonArray);
        }
    }
}
