/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

/**
 * The package provides core abstractions used in any commercial application. The cornerstone of the entities is the comment and tags classes providing a simple
 * and powerful extension mechanisms for regular abstractions.
 * <p>Core domain model abstractions such as postal address, email address, or bank connection are provided with validation routines.</p>
 * <p>A set of mixin interfaces to mark entities supporting unique object identifier, external identifiers, names, tags and comments.
 * <p>Through iterations the optimal mixins were identified. The _HasOid_ marks an instance internally stored in a repository. The _HasId_ marks an instance
 * shared with other bounded domains and having a unique public identifier. The _HasName_ marks an instance with human-readable identification characteristic. A
 * name is often not unique and also used for entities without a recognized external identifier.</p>
 */
package net.tangly.core;
