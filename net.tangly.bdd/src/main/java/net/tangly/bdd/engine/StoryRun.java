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

import net.tangly.bdd.Feature;
import net.tangly.bdd.Scene;
import net.tangly.bdd.Story;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recall that our custom extension generates BDD reports after executing the tests. Some parts of these reports are pulled from the elements of the
 * “@Story” annotation. We use the beforeAll callback to store these strings. Later, at the end of the execution lifecycle, we retrieve these strings
 * to generate reports. A simple POJO is used for this purpose. We name this class “StoryDetails”. The following code snippet demonstrates the process
 * of creating an instance of this class and save the elements of the annotation into the instance.
 */
class StoryRun {
    private final Class<?> clazz;
    private final List<Scene> scenes;

    public StoryRun(Class<?> clazz) {
        this.clazz = clazz;
        scenes = new ArrayList<>();
    }

    // region Story

    public String name() {
        Story story = clazz.getAnnotation(Story.class);
        return story.value();
    }

    public String id() {
        Story story = clazz.getAnnotation(Story.class);
        return story.id();
    }

    public String description() {
        Story story = clazz.getAnnotation(Story.class);
        return story.description();
    }

    public Class<?> clazz() {
        return clazz;
    }

    // endregion

    // region Feature

    public String featureName() {
        Feature feature = clazz.getPackage().getAnnotation(Feature.class);
        return feature.value();
    }

    public String featureId() {
        Feature feature = clazz.getPackage().getAnnotation(Feature.class);
        return feature.id();
    }

    public String featureDescription() {
        Feature feature = clazz.getPackage().getAnnotation(Feature.class);
        return feature.description();
    }

    public Package packages() {
        return clazz.getPackage();
    }

    // endregion

    // region Scene

    public void addScene(Scene scene) {
        scenes.add(scene);
    }

    public List<Scene> scenes() {
        return Collections.unmodifiableList(scenes);
    }

    public Scene findSceneByMethodName(String name) {
        return scenes.stream().filter(o -> name.equals(o.methodName())).findAny().orElse(null);
    }

    // endregion
}
