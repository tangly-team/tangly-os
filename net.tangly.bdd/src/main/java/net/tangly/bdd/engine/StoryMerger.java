/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.bdd.engine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merges a set of JSON files produced through the execution of BDD JUnit tests. A feature is part of exactly one package. Therefore, the package name
 * is the key to the feature. A story is part of exactly one class. Therefore, the class name is the key for the story. A scenario is part of exactly
 * one method.
 */
public class StoryMerger {
    /**
     * /** The logger of the instance.
     */
    private static final Logger logger = LoggerFactory.getLogger(StoryMerger.class);

    private final JSONArray features;

    public StoryMerger() {
        features = new JSONArray();
    }

    public void merge(Path directory) throws IOException {
        try (Stream<Path> results = Files.walk(directory)) {
            results.filter(t -> t.toString().endsWith(".json")).forEach(t -> {
                try (Reader reader = Files.newBufferedReader(t, StandardCharsets.UTF_8)) {
                    JSONArray report = new JSONArray(new JSONTokener(reader));
                    JSONObject feature = (JSONObject) report.get(0);
                    merge(feature);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (JSONException e) {
                    logger.atInfo().log("File {} is incorrect due to parallel writing", t);
                }
            });
        }
    }

    public void write(Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(features.toString(4));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void merge(JSONObject feature) {
        Optional<JSONObject> mergedFeature = contains(features, Constants.PACKAGE_NAME, feature.getString(Constants.PACKAGE_NAME));
        if (mergedFeature.isEmpty()) {
            features.put(feature);
        } else {
            JSONArray mergedStories = mergedFeature.get().getJSONArray(Constants.STORIES);
            JSONArray stories = feature.getJSONArray(Constants.STORIES);
            for (var item : stories) {
                JSONObject story = (JSONObject) item;
                if (contains(mergedStories, Constants.CLASS_NAME, story.getString(Constants.CLASS_NAME)).isEmpty()) {
                    mergedStories.put(story);
                }
            }
        }
    }

    public JSONArray features() {
        return features;
    }

    private Optional<JSONObject> contains(JSONArray list, String key, String value) {
        for (var item : list) {
            JSONObject object = (JSONObject) item;
            if (object.has(key) && (object.getString(key).equals(value))) {
                return Optional.of(object);
            }
        }
        return Optional.empty();
    }
}
