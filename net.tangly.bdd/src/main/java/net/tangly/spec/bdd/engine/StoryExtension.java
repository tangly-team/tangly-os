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
 *
 */

package net.tangly.spec.bdd.engine;

import net.tangly.spec.bdd.Scenario;
import net.tangly.spec.bdd.Scene;
import net.tangly.spec.bdd.Story;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

/**
 * A custom extension that allows test authors to create and run behaviors and stories i.e. BDD specification tests.
 * <p>Jupiter engine will provide an execution context instance under which an extension
 * is to operate. A store is a holder that can be used by custom extensions to save and retrieve arbitrary data––basically a supercharged in-memory map. In order to avoid
 * accidental key collisions between multiple extensions, the good folk at JUnit introduced the concept of a namespace. A namespace is a way to scope the data saved by
 * extensions.</p>
 */
public class StoryExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(StoryExtension.class);

    public StoryExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!isStory(context)) {
            throw new IllegalStateException("Use @Story annotation to use Story Extension. Class: " + context.getRequiredTestClass());
        }
        var clazz = context.getRequiredTestClass();
        var storyRun = new StoryRun(clazz);
        context.getStore(NAMESPACE).put(clazz.getName(), storyRun);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (isStory(context)) {
            new StoryWriter(storyDetails(context)).write();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (!isScenario(context)) {
            throw new IllegalStateException("Use @Scenario annotation to use the StoryExtension service. Method: " + context.getRequiredTestMethod());
        }
        // Prepare a scene instance corresponding to the given test method.
        var scene = new Scene(context.getRequiredTestMethod());
        storyDetails(context).addScene(scene);
    }

    @Override
    public boolean supportsParameter(@NotNull ParameterContext parameterContext, ExtensionContext extensionContext) {
        return Scene.class.equals(parameterContext.getParameter().getType());
    }

    /**
     * Inject the previously created scene object into the test method.
     *
     * @param parameterContext parameter context injected by JUnit5 lifecycle
     * @param context          context injected by JUnit5 lifecycle
     * @return story run object associated with the story under execution
     */
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) {
        return storyDetails(context).findSceneByMethodName(context.getRequiredTestMethod().getName());
    }

    private static StoryRun storyDetails(ExtensionContext context) {
        Class<?> clazz = context.getRequiredTestClass();
        return context.getStore(NAMESPACE).get(clazz.getName(), StoryRun.class);
    }

    private static boolean isStory(ExtensionContext context) {
        return isAnnotated(context.getRequiredTestClass(), Story.class);
    }

    private static boolean isScenario(ExtensionContext context) {
        return isAnnotated(context.getRequiredTestMethod(), Scenario.class);
    }
}
