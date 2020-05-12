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

package net.tangly.bdd;

import net.tangly.bdd.engine.StoryAsciiDocPublisher;
import net.tangly.bdd.engine.StoryMerger;
import net.tangly.bdd.engine.StoryWriter;
import org.junit.jupiter.api.AfterAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class StoreTest {
    @AfterAll
    public static void mergeStories() throws IOException {
        Path bddReportsFolder = StoryWriter.getOrCreateBddReportsFolder(StoreTest.class);
        Path bddReports = bddReportsFolder.resolve(Path.of("bdd-features.json"));
        Files.deleteIfExists(bddReports);

        StoryMerger merger = new StoryMerger();
        merger.merge(bddReportsFolder);
        merger.write(bddReports);

        Path bddPublished = bddReportsFolder.resolve(Path.of("bdd-report.adoc"));
        new StoryAsciiDocPublisher(bddReports, bddPublished);
    }

    public Store create(Scene scene, int blackSweaters, int blueSweaters) {
        Store store = new Store(blackSweaters, blueSweaters);
        scene.put("store", store);
        return store;
    }

    public Store store(Scene scene) {
        return scene.<Store>get("store");
    }

}
