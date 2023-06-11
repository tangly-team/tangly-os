/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.core;

/**
 * The named interface defines a powerful abstraction for entities of a domain model. The features are:
 * <dl>
 * <dt>oid</dt><dd>An internal identifier owned by the domain. The internal identifier should never visible to external systems or users.</dd>
 * <dt>id</dt><dd>An external identifier used to identify the entity inside and outside the domain.
 * The ownership fo the external identifier could be an external system.</dd>
 * <dt>name</dt><dd>A human readable name used to identify the entity when humans are involved.
 * The uniqueness of the name is not required or guaranteed.</dd>
 * <li>text</dt><dd>a human readable documentation of the instance. We recommend using markdown syntax for the text.</dd>
 * <dt>comments</dt><dd>human readable comments describing aspects of an instance.</dd>
 * <dt>tags</dt><dd>human readable and machine processable tags defining an ontology to classify instance in the domain model.
 * Orthogonal information can therefore be attached to instances.</dd>
 * </dl>
 */
public interface Entity extends HasOid, HasId, HasName, HasText, HasTimeInterval, HasTags, HasComments {
    /**
     * Check if the entity is consistent based on the fields values and business rules.
     *
     * @return true if the entity is consistent otherwise false
     */
    default boolean validate() {
        return true;
    }
}
