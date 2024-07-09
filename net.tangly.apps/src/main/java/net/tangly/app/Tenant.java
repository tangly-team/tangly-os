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
import net.tangly.app.ports.AppsAdapter;
import net.tangly.app.ports.AppsEntities;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.app.services.AppsBusinessLogic;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.UsersProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * A tenant has a set of bounded domains. Each bounded domain can have a user interface or a REST interface.
 * A tenant has a set of users and roles. The tenant is the root of the security model.
 * Persistent data is stored on a per-tenant basis.
 * <p>A tenant is configured through the properties file passed to the constructor.</p>
 */
public class Tenant {
    public static final String IN_MEMORY_PROPERTY = "tenant.in-memory";
    public static final String DATABASES_DIRECTORY_PROPERTY = "tenant.root.db.directory";
    public static final String IMPORTS_DIRECTORY_PROPERTY = "tenant.root.imports.directory";
    public static final String REPORTS_DIRECTORY_PROPERTY = "tenant.root.reports.directory";
    private static final Logger logger = LogManager.getLogger();
    private final String id;
    private final Properties properties;
    private final TypeRegistry registry;
    private final Map<String, BoundedDomain<?, ?, ?>> boundedDomains;
    private final Map<String, BoundedDomainRest> boundedDomainRests;

    public Tenant(@NotNull String id, InputStream properties) {
        this.id = id;
        this.properties = new Properties();
        if (Objects.nonNull(properties)) {
            try {
                this.properties.load(properties);
            } catch (IOException e) {
                logger.atError().log("Tenant configuration properties load error {}", e);
                throw new RuntimeIOException(e);
            }
        }
        this.registry = new TypeRegistry();
        boundedDomains = new HashMap<>();
        boundedDomainRests = new HashMap<>();
        ofAppDomain();
    }

    public UsersProvider UsersProviderFor(@NotNull String domain) {
        return UsersProvider.of(() -> apps().logic().usersFor(domain), () -> apps().logic().activeUsersFor(domain));
    }

    public String id() {
        return id;
    }

    /**
     * Initializes the tenant upon creation. Upon completion, the tenant is ready to be used in the application.
     * Tenants are created when the first user of the tenant logs in.
     */
    public void startup(@NotNull Javalin javalin) {
        boundedDomains.values().forEach(BoundedDomain::startup);
        boundedDomainRests.values().forEach(o -> o.registerEndPoints(javalin));
    }

    /**
     * Shutdowns the tenant before unloading the tenant from the application.
     * Tenants are shutdown when the last user of the tenant logs out.
     */
    public void shutdown() {
        boundedDomains.values().forEach(BoundedDomain::shutdown);
    }

    public Properties properties() {
        return properties;
    }

    public TypeRegistry registry() {
        return registry;
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

    public AppsBoundedDomain apps() {
        return (AppsBoundedDomain) boundedDomains.get(AppsBoundedDomain.DOMAIN);
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
        return Boolean.parseBoolean(properties().getProperty(IN_MEMORY_PROPERTY, "true"));
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

    private void ofAppDomain() {
        var realm = inMemory() ? new AppsEntities() : new AppsEntities(Path.of(databases(), AppsBoundedDomain.DOMAIN));
        var domain = new AppsBoundedDomain(this, realm, new AppsBusinessLogic(realm), new AppsAdapter(realm, Path.of(imports(AppsBoundedDomain.DOMAIN))),
            registry());
        registerBoundedDomain(domain);
    }
}
