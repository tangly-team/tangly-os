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

package net.tangly.bdd.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.tangly.bdd.Scene;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Writes the story with all executed scenarios in a JSON file. The feature associated with the package of the story class is also added to the
 * generated file.
 */
public class StoryWriter {
    private static final Set<String> knownTargetDirs = Set.of("target", "build", "out");
    private final StoryRun run;
    private final Path path;

    public StoryWriter(@NotNull StoryRun run) {
        this.run = run;
        this.path = Paths.get(getOrCreateBddReportsFolder(run.getClass()).toString(), run.clazz().getName() + Constants.EXT);
    }

    public static Path getOrCreateBddReportsFolder(@NotNull Class<?> clazz) {
        URL url = clazz.getResource("");
        Objects.requireNonNull(url);
        Path targetDir = Paths.get(url.getPath());
        int index = targetDir.getNameCount() - 1;
        while ((index > 0) && (!knownTargetDirs.contains(targetDir.getName(index).toString()))) {
            --index;
            targetDir = targetDir.getParent();
        }
        Path bddReportsDirectory = targetDir.resolve(Paths.get(Constants.BDD_REPORTS_FOLDER));
        File bddReports = bddReportsDirectory.toFile();
        if (!bddReports.exists() && !bddReports.mkdir()) {
            throw new RuntimeException("Unable to create the folder for saving bdd reports.");
        }
        return bddReportsDirectory;
    }

    void write() {
        // write feature
        JSONArray features = new JSONArray();
        JSONObject feature = createFeature(run);
        features.put(feature);

        // write story
        JSONObject story = createStory(run);
        feature.getJSONArray(Constants.STORIES).put(story);

        // write scenario
        for (Scene scene : run.scenes()) {
            story.getJSONArray(Constants.SCENARIOS).put(createScenario(scene));
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(features.toString(4));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JSONObject createFeature(StoryRun run) {
        JSONObject feature = new JSONObject();
        feature.put(Constants.PACKAGE_NAME, run.packages().getName());
        feature.put(Constants.NAME, run.featureName());
        feature.put(Constants.ID, run.featureId());
        feature.put(Constants.DESCRIPTION, run.featureDescription());
        feature.put(Constants.STORIES, new JSONArray());
        return feature;
    }

    private static JSONObject createStory(StoryRun run) {
        JSONObject story = new JSONObject();
        story.put(Constants.CLASS_NAME, run.clazz().getName());
        story.put(Constants.NAME, run.name());
        story.put(Constants.ID, run.id());
        story.put(Constants.DESCRIPTION, run.description());
        story.put(Constants.SCENARIOS, new JSONArray());
        return story;
    }

    private static JSONObject createScenario(@NotNull Scene scene) {
        JSONObject scenario = new JSONObject();
        scenario.put(Constants.METHOD_NAME, scene.methodName());
        scenario.put(Constants.NAME, scene.description());
        if (!scene.given().text().isBlank()) {
            JSONObject given = new JSONObject();
            given.put(Constants.TEXT, scene.given().text());
            addAnds(scene.given().ands(), given);
            scenario.put(Constants.GIVEN, given);
        }
        if (!scene.when().text().isBlank()) {
            JSONObject when = new JSONObject();
            when.put(Constants.TEXT, scene.when().text());
            scenario.put(Constants.WHEN, when);
        }
        JSONObject then = new JSONObject();
        then.put(Constants.TEXT, scene.then().text());
        addAnds(scene.then().ands(), then);
        scenario.put(Constants.THEN, then);
        return scenario;
    }

    private static void addAnds(@NotNull List<String> ands, @NotNull JSONObject jsonObject) {
        if (!ands.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            ands.forEach(jsonArray::put);
            jsonObject.put(Constants.AND, jsonArray);
        }
    }
}
