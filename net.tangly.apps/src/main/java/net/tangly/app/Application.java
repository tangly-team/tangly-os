/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.app;

import io.javalin.Javalin;
import net.tangly.app.api.BoundedDomainRest;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * The application defines the context of the whole digital product with a set of bounded domains and their ports.
 * The bounded domains with their ports are the entry points to the application. The application is a singleton.
 * The ports are the user interface and the REST services. Ports are optional.
 */
public final class Application {
    public static final String IN_MEMORY_PROPERTY = "application.in-memory";
    public static final String DATABASES_DIRECTORY_PROPERTY = "application.root.db.directory";
    public static final String IMPORTS_DIRECTORY_PROPERTY = "application.root.imports.directory";
    public static final String REPORTS_DIRECTORY_PROPERTY = "application.root.reports.directory";
    private static Application self = new Application();
    private static final Logger logger = LogManager.getLogger();
    private final Properties properties;
    private final TypeRegistry registry;
    private final Map<String, BoundedDomain<?, ?, ?>> boundedDomains;
    private final Map<String, BoundedDomainRest> boundedDomainRests;

    public static Application instance() {
        return self;
    }

    public Application() {
        this.properties = new Properties();
        this.registry = new TypeRegistry();
        boundedDomains = new HashMap<>();
        boundedDomainRests = new HashMap<>();
        try (var in = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            logger.atError().log("Application configuration properties load error {}", e);
            throw new RuntimeIOException(e);
        }
    }

    public Properties properties() {
        return properties;
    }

    public TypeRegistry registry() {
        return registry;
    }

    public void initializeBoundedDomainRests(@NotNull Javalin javalin) {
        boundedDomainRests.values().forEach(o -> o.registerEndPoints(javalin));
    }

    public Map<String, BoundedDomain<?, ?, ?>> boundedDomains() {
        return Collections.unmodifiableMap(boundedDomains);
    }

    public Map<String, BoundedDomainRest> boundedDomainRests() {
        return Collections.unmodifiableMap(boundedDomainRests);
    }

    public void registerBoundedDomain(BoundedDomain<?, ?, ?> domain) {
        boundedDomains.put(domain.name(), domain);
    }

    public Optional<BoundedDomain<?, ?, ?>> getBoundedDomain(String name) {
        return Optional.ofNullable(boundedDomains.get(name));
    }

    public void registerBoundedDomainRest(BoundedDomainRest domain) {
        boundedDomainRests.put(domain.name(), domain);
    }

    public Optional<BoundedDomainRest> getBoundedDomainRest(String name) {
        return Optional.ofNullable(boundedDomainRests.get(name));
    }

    public boolean isEnabled(@NotNull String domain) {
        return Boolean.parseBoolean(properties().getProperty(STR."\{domain}.enabled", "true"));
    }

    public boolean inMemory() {
        return Boolean.parseBoolean(properties().getProperty("application.in-memory", "true"));
    }

    public String getProperty(String property) {
        return properties().getProperty(property);
    }

    public String imports(String domain) {
        return STR."\{getProperty(IMPORTS_DIRECTORY_PROPERTY)}/\{domain}";
    }

    public String databases() {
        return getProperty(DATABASES_DIRECTORY_PROPERTY);
    }

    public String reports(String domain) {
        return STR."\{getProperty(REPORTS_DIRECTORY_PROPERTY)}/\{domain}";
    }
}
