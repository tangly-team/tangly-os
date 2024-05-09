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
 * The package defines the core abstractions for a bounded domain implementation. Entities defined in the domain can have oid, id, name, tags and comments.
 * The realm interface is the entry point for the repository and factory abstractions.
 * Repositories retrieve domain objects from persistence technologies.
 * Factories create new object trees. The regular persistence approach is based on EclipseStore.
 * The solution is well integrated in Java so that retrieval and creation operations are simple to implement.
 * <p>The key abstractions are:</p>
 * <dl>
 *     <dt>Bounded Domain</dt><dd>Defines the bounded domain model and the domain services.</dd>
 *     <dt>Realm</dt><dd>Defines the repositories interfance to retrieve all domein entities.</dd>
 *     <dt>Port</dt><dd>Port interface for all inbound and outpound communication.</dd>
 * </dl>
 * <p>The user interface is an optional port. We support Vaadin based user interfaces.</p>
 * <p>The REST interface is an optional port. We use Javalin and OpenAPI to build a REST interface to the domain.</p>
 */
package net.tangly.core.domain;
