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

package net.tangly.spec.bdd.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Merges a set of JSON files produced through the execution of BDD JUnit tests. A feature is part of exactly one package. Therefore, the package name is the
 * key to the feature. A story is part of exactly one class. Therefore, the class name is the key for the story. A scenario is part of exactly one method.
 */
public class StoryMerger {
    private static final Logger logger = LogManager.getLogger();
    private final JSONArray features;

    public StoryMerger() {
        features = new JSONArray();
    }

    /**
     * Merges all BDD reports in the given directory into one file.
     *
     * @param directory directory containing all feature reports to be merged
     * @throws IOException if access to the file system encountered a problem
     */
    public void merge(@NotNull Path directory) throws IOException {
        try (Stream<Path> results = Files.walk(directory)) {
            results.filter(t -> t.toString().endsWith(".json")).forEach(t -> {
                try (Reader reader = Files.newBufferedReader(t, StandardCharsets.UTF_8)) {
                    var report = new JSONArray(new JSONTokener(reader));
                    merge((JSONObject) report.get(0));
                } catch (IOException e) {
                    logger.atError().withThrowable(e).log("File {} IO error ", t);
                    throw new UncheckedIOException(e);
                } catch (JSONException e) {
                    logger.atError().withThrowable(e).log("File {} is incorrect due to parallel writing", t);
                }
            });
        }
    }

    private static Optional<JSONObject> contains(JSONArray list, String key, String value) {
        for (var item : list) {
            var object = (JSONObject) item;
            if (object.has(key) && (object.getString(key).equals(value))) {
                return Optional.of(object);
            }
        }
        return Optional.empty();
    }

    /**
     * Writes the features to the provided file.
     *
     * @param path path of the file where the features are archived
     */
    public void write(Path path) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(features.toString(4));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void merge(JSONObject feature) {
        Optional<JSONObject> mergedFeature = contains(features, BddConstants.PACKAGE_NAME, feature.getString(BddConstants.PACKAGE_NAME));
        if (mergedFeature.isEmpty()) {
            features.put(feature);
        } else {
            var mergedStories = mergedFeature.get().getJSONArray(BddConstants.STORIES);
            var stories = feature.getJSONArray(BddConstants.STORIES);
            for (var item : stories) {
                var story = (JSONObject) item;
                if (contains(mergedStories, BddConstants.CLASS_NAME, story.getString(BddConstants.CLASS_NAME)).isEmpty()) {
                    mergedStories.put(story);
                }
            }
        }
    }
}
