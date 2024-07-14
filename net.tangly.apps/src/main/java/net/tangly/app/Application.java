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

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The application defines the context of the whole digital product with a set of tenants.
 * Each tenant has a set of bounded domains and their ports. Each tenant has an own user management and type registry.
 * Business logic tailoring shall mainly be done through the type registry with their set of custom tags and codes.
 * The bounded domains with their ports are the entry points to the application. The application is a singleton.
 * The ports are the user interface and the REST services. Ports are optional.
 * <p> The application shall be created in the main method of the application. The main method is responsible to insure that
 * the constructor is called exactly once to insure the singleton pattern. The main application is single threaded per convention.</p>
 */
public final class Application {
    private static Application self = new Application();

    private final Map<String, Tenant> tenants;

    public static Application instance() {
        return self;
    }

    public Application() {
        tenants = new HashMap<>();

    }

    public void putTenant(@NotNull Tenant tenant) {
        tenants.put(tenant.id(), tenant);
    }

    public void removeTenant(@NotNull String id) {
        tenants.remove(id);
    }

    public Tenant tenant(@NotNull String id) {
        return tenants.get(id);
    }

    public Collection<Tenant> tenants() {
        return tenants.values();
    }
}
