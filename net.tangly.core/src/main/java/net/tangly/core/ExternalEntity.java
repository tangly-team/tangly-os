/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

/**
 * The named interface defines a powerful abstraction for external entities in a domain model. The features are
 * <ul>
 * <li>oid: a mandatory external identifier, an external identifier is visible to external systems or users. </li>
 * <li>text: a human readable documentation of the instance. We recommend using markdown syntax for the text.</li>
 * <li>comments: human readable comments describing aspects of an instance.</li>
 * <li>tags: human readable and machine processable tags defining an ontology to classify instance in the domain model.
 * Orthogonal information can therefore be attached to instances.</li>
 * </ul>
 * <p> The external identifier shall be set at construction time.</p>
 */
public interface ExternalEntity extends HasId, HasName, HasText, HasDate, HasTags, HasComments {
}
