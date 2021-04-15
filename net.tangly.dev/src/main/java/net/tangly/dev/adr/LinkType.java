/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.dev.adr;

/**
 * Defines the types of links between architecture design records.
 * <dl>
 *     <dt>supersedes</dt><dd>a design record supersedes another design record.</dd>
 *     <dt>supersededBy</dt><dd>defines the symmetrical relation to a supersedes link. A design record is replaced by a new one.</dd>
 *     <dt>references</dt><dd>defines an unspecified asymmetrical link between two design records.</dd>
 * </dl>
 */
public enum LinkType {
    supersedes, supersededBy, references
}
