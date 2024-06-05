/*
 * Copyright 2006-2024 Marcel Baumann
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
 * The package defines the core abstractions for a bounded domain implementation.
 * Entities defined in the domain can have oid, id, name, tags and comments.
 * The realm interface is the entry point for the repository and factory abstractions.
 * Repositories retrieve domain objects from persistence technologies.
 * Factories create new object trees. The regular persistence approach is based on EclipseStore.
 * The solution is well integrated in Java so that retrieval and creation operations are straightforward to implement.
 * <p>The key abstractions are:</p>
 * <dl>
 *     <dt>Bounded Domain</dt><dd>Defines the bounded domain model and the domain services.</dd>
 *     <dt>Realm</dt><dd>Defines the repositories interfaces to retrieve all domain entities.</dd>
 *     <dt>Port</dt><dd>Port interface for all inbound and outbound communication.</dd>
 * </dl>
 * <p>The user interface is an optional port. We support Vaadin based user interfaces.</p>
 * <p>The REST interface is an optional port. We use Javalin and OpenAPI to build a REST interface to the domain.</p>
 * <h2>Creating a New Bounded Domain</h2>
 * <dl>
 * <dt>Define Domain Entities</dt><dd>The domain entities are defined as regular Java classes and records.
 * Per convention all classes are defined in the package DOMANIN_NAME.domain.</dd>
 * <dt>Define the interfaces to the Bounded Domain</dt><dd>
 *     <p>Per convention all classes are defined in the package DOMAIN.services</p>
 *     <ul>
 *         <li>The bounded domain class DOMAINBoundedDomain defines all the instances defining the bounded domain.
 *         It implements the {@link net.tangly.core.domain.BoundedDomain} interface.</li>
 *         <li>The realm DOMAINRealm defines the repository interface. It implements the {@link net.tangly.core.domain.Realm} interface.</li>
 *         <li>The port DOMAINPort defies the default port interface. It implements the {@link net.tangly.core.domain.Port} interface.
 *         This interface defines the import, export and clear functions for the bounded domain.</li>
 *        <li>The optional DOMAINBusinesLogic class defines business logic functions. It does not implement any specific interface.</li>
 *     </ul>
 * </dd>
 * <dt>Define the implementation of all service interfaces</dt><dd>
 *     <p>Per convention all classes are defined in the package DOMAIN.ports.</p>
 *     <ul>
 *         <li>Implement the realm interface. Per convention the class is called DOMAINEntities.
 *         Use the {@link net.tangly.core.providers.Provider} abstractions to provide persistence with EclipseStore.</li>
 *         <li>Implement the port interface. Per convention the class is called DOMAINAdapter.</li>
 *         <li>Define the TSV Handler to enalbe the adapter to import entities from a TSV file and export them to a TSV file.</li>
 *     </ul>
 * </dd>
 * </dl>
 *
 * <h2>Realm</h2>
 * <p>The realm persists domain entities and aggregates.
 * The preferred approach uses EclipseStore to persist graphs of Java objects.
 * The provider interface and their various implementation is a powerful mechanism to store and retrieve domain entities.</p>
 * <h2>Port</h2>
 * <p><The port provides the interface to the domain model for the adapters.
 * Any bounded domain provides an import, export, and clear operations.
 * The import loads an archive of all entities of the domain.
 * The export archives all entities to a set of files.
 * The clear removes all entities from the doamin.
 * THe preferred import and export format is TSV>/p>
 * <h2>TSV Import and Export</h2>
 * <p>Utilities are provided in the Gleam module. </p>
 * You can use the TSV import and export utilities to import and export entities from and to TSV files.
 * <h2>Business Logic</h2>
 * <p></p>
 * <h2>Vaadin User Interface</h2>
 * <p></p>
 * <h2>REST Interface</h2>
 * <p></p>
 * <h2>Domain Events</h2>
 * <p></p>
 */
package net.tangly.core.domain;
