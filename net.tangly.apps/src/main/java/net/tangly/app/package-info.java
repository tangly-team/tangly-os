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

/**
 * Provides the abstractions to create a full-fledged Vaadin application with user interface and optional REST interface for a set of bounded domains.
 * The application is the entry point for the application and orchestrates the creation of tenants and the user interface.
 * <h2>Application</h2>
 * The application is the entry point for the application and orchestrates the creation of tenants and the user interface.
 * An application is composed of a set of bounded domains. An application supports a set of tenants.
 * The application is a singleton.
 * <h2>Tenant</h2>
 * A tenant has separate data stores and has no access to the data of other tenants. The tenant is the root of the bounded domains and the user interface.
 * Configuration of bounded domains is done in the tenant.
 * Each bounded domain loads the configuration data from the tenant at startup.
 * <p>The login process requests an identification <code>tenant/username</code>. The tenant is used to select the tenant information and the user belonging
 * to its organization. The username and password are used to authenticate the user in the context of the tenant.</p>
 * <h2>Application View</h2>
 * The application view is the user interface of the application. It is composed of a set of views for the bounded domains.
 * A new application view instance is created for each connected user. The creation is performed by the Vaadin framework.
 */
package net.tangly.app;
