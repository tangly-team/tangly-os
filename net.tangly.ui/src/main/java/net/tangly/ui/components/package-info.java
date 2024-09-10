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
 * Mvn install can use the application (once) mvn (to run the application) The package provides a CRUD domain for entities. Entities can have
 * <ul>
 *     <li>A list displaying a list of entities with selected fields.</li>
 *     <li>A tabbed details view showing simple fields view, comments view, tags view. The comments and tags view display again a list of
 *     values.</li>
 *     <li>All custom components are extended to support setting a null value. The syntax is set a new value or clear the content.</li>
 * </ul>
 * <p>The composite field abstractions cover two scenarios. Composite fields for immutable entities cannot use a binder. We provide a validator method to return
 * validation logic for all internal fields. Regular beans providing at least getters for fields can be connected with a binder. Here we provide a
 * binding method to connect the bean fields with converters and validators declared in the binder.</p>
 * <p>Care was taken to support cascading read-only and editing modes for all details views.</p>
 */

package net.tangly.ui.components;
