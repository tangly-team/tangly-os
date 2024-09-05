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
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.TenantDirectory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

/**
 * A tenant has a set of bounded domains specific to an organizational entity. Each bounded domain can have a user interface or a REST interface.
 * A tenant has a set of users and roles. Authorization rights are declared per uer and per bounded domain. The tenant is the root of the security model.
 * Persistent data is stored on a per-tenant basis.
 * <p>A tenant is configured through the properties file passed to the constructor. The identifier of the tenant is used to select the organization during
 * the authentication process.</p>
 * <p>Tenants have no access to other tenants defined in the application.</p>
 */
public class Tenant implements TenantDirectory {
    public static final String IN_MEMORY_PROPERTY = "tenant.in-memory";
    public static final String TENANT_ROOT_DIRECTORY_PROPERTY = "tenant.root.directory";
    public static final String DATABASES_DIRECTORY_PROPERTY = "tenant.root.db.directory";
    public static final String IMPORTS_DIRECTORY_PROPERTY = "tenant.root.imports.directory";
    public static final String DOCUMENTS_DIRECTORY_PROPERTY = "tenant.root.docs.directory";
    private static final Logger logger = LogManager.getLogger();
    private final String id;
    private final Properties properties;
    private final Map<String, BoundedDomain<?, ?, ?>> boundedDomains;
    private final Map<String, BoundedDomainRest> boundedDomainRests;

    public Tenant(@NotNull Properties properties) {
        Objects.requireNonNull(properties.get(TENANT_ID_PROPERTY), "The tenant id is mandatory");
        this.id = properties.getProperty(TENANT_ID_PROPERTY);
        this.properties = properties;
        boundedDomains = new HashMap<>();
        boundedDomainRests = new HashMap<>();
        ofAppDomain();
    }

    // region Tenant Directory

    @Override
    public String id() {
        return id;
    }

    @Override
    public String getProperty(@NotNull String property) {
        return properties.getProperty(property);
    }

    @Override
    public Collection<BoundedDomain<?, ?, ?>> boundedDomains() {
        return boundedDomains.values();
    }

    @Override
    public Optional<BoundedDomain<?, ?, ?>> getBoundedDomain(@NotNull String name) {
        return Optional.ofNullable(boundedDomains.get(name));
    }

    @Override
    public List<String> usersFor(@NotNull String domain) {
        return apps().logic().usersFor(domain);
    }

    @Override
    public List<String> activeUsersFor(@NotNull String domain) {
        return apps().logic().activeUsersFor(domain);
    }

    @Override
    public String docs(@NotNull String domain) {
        return "%s/%s".formatted(getProperty(DOCUMENTS_DIRECTORY_PROPERTY), domain);
    }



    // endregion

    public boolean isEnabled(@NotNull String domain) {
        return Boolean.parseBoolean(properties.getProperty("%s.enabled".formatted(domain), "true"));
    }

    public Properties properties() {
        return properties;
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

    public Map<String, BoundedDomainRest> boundedDomainRests() {
        return Collections.unmodifiableMap(boundedDomainRests);
    }

    public void registerBoundedDomain(BoundedDomain<?, ?, ?> domain) {
        boundedDomains.put(domain.name(), domain);
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

    public boolean inMemory() {
        return Boolean.parseBoolean(properties.getProperty(IN_MEMORY_PROPERTY, "true"));
    }

    public String imports(String domain) {
        return "%s/%s".formatted(getProperty(IMPORTS_DIRECTORY_PROPERTY), domain);
    }

    public String resources(String domain) {
        return "%s/%s".formatted(imports(domain), "resources");
    }

    public String databases() {
        return getProperty(DATABASES_DIRECTORY_PROPERTY);
    }

    private void ofAppDomain() {
        var realm = inMemory() ? new AppsEntities() : new AppsEntities(Path.of(databases(), AppsBoundedDomain.DOMAIN));
        var domain = new AppsBoundedDomain(this, realm, new AppsBusinessLogic(realm), new AppsAdapter(realm, Path.of(imports(AppsBoundedDomain.DOMAIN))), this);
        registerBoundedDomain(domain);
    }
}
