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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class StoryAsciiDocPublisher {
    public static final String NEWLINE = " +" + System.lineSeparator();

    private final PrintWriter writer;

    public StoryAsciiDocPublisher(Path bddReport, Path report) throws IOException {
        try (Reader reader = Files.newBufferedReader(bddReport, StandardCharsets.UTF_8)) {
            JSONArray features = new JSONArray(new JSONTokener(reader));
            writer = new PrintWriter(report.toFile(), StandardCharsets.UTF_8);
            header();
            for (var item : features) {
                publishFeature((JSONObject) item);
            }
            writer.flush();
            writer.close();
        }
    }

    private void header() {
        writer.println("---");
        writer.println("title: \"bdd Report\"");
        writer.println("date: 2020-01-01");
        writer.println("weight: 60");
        writer.println("draft: false");
        writer.println("---");
        writer.println();
    }

    private void publishFeature(JSONObject feature) {
        header(feature.getString(Constants.NAME), 2);
        paragraph(feature.getString(Constants.DESCRIPTION));
        for (var item : feature.getJSONArray(Constants.STORIES)) {
            publishStory((JSONObject) item);
        }
    }

    private void publishStory(JSONObject story) {
        header(story.getString(Constants.NAME), 3);
        paragraph(story.getString(Constants.DESCRIPTION));
        for (var item : story.getJSONArray(Constants.SCENARIOS)) {
            publishScenario((JSONObject) item);
        }
    }

    private void publishScenario(JSONObject scenario) {
        header(scenario.getString(Constants.NAME), 4);
        writer.println("[%hardbreaks]");
        clause(scenario, Constants.GIVEN);
        clause(scenario, Constants.WHEN);
        clause(scenario, Constants.THEN);
        writer.println();
    }

    private void clause(JSONObject scenario, String clause) {
        JSONObject segment = (JSONObject) scenario.get(clause);
        writer.append("*").append(clause).append("* ").append(segment.getString(Constants.TEXT));
        JSONArray ands = segment.optJSONArray(Constants.AND);
        if (ands != null) {
            for (var item : ands) {
                writer.append(" *and* ").append((String) item);
            }
        }
        writer.println();
    }

    private void header(String text, int level) {
        writer.append("=".repeat(level)).append(" ").append(text).println();
        writer.println();
    }

    private void paragraph(String text) {
        writer.append(text).println();
        writer.println();
    }
}
