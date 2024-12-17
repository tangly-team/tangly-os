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
 * Provides the provider abstraction to handle a list of items. Update operations are supported. The provided abstractions are
 * <dl>
 *     <dt>Provider</dt><dd>defines the interface for all providers. It defines the CRUD operations. The create and update operations are merged.
 *     It can model a value baed object collection.</dd>
 *     <dt>Provider In Memory</dt><dd>provides an implementation where the items are stored in memory and never persisted.</dd>
 *     <dt>Provider Persistence</dt><dd>provides an implementation where the items are persisted.</dd>
 *     <dt>Provider Has Oid</dt><dd>provides a decorator for provider holding items with unique object identifiers. Such objects must implements the HasOid
 *     <dt>Provider Has Id</dt><dd>provides a decorator for provider holding items with unique identifiers. Such objects must implements the HasId
 *     interface.</dd>
 * </dl>
 * <p>How can you provide basis operation for ACID transactions.</p>
 */
package net.tangly.core.providers;
