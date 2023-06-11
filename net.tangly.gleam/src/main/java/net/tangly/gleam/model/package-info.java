/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

/**
 * Provides the abstraction to configure programmatically the declarative mapping of business entities to various ports. Currently we support mappings to
 * <ul>
 *     <li>TSV - Tab Separated Values - to archive or retrieve Java entities mapped to a TSV file.</li>
 *     <li>JSON - Javascript Simple Object Notation - to archive or retrieve Java entities mapped to a JSON file.</li>
 *     <li>Naked Objects with Vaadin - under definition and not ready for use.</li>
 * </ul>
 * <p>Import and export of business entities are supported through these frameworks.</p>
 */
package net.tangly.gleam.model;
