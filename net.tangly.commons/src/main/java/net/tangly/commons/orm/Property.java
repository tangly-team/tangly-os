/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.orm;

import net.tangly.commons.models.HasOid;

/**
 * Models the common attributes of an entity property. Conceptually three kinds of properties exist:
 * <ul>
 * <li>A property referencing a Java object or a Java primitive type. The object can be a single value or a collection. Single value object can
 * either be persisted through standard JDBC mechanisms or through provided Function[S,T] transformers.</li>
 * <li>A single value property referencing another persisted entity persisted through a DAO.</li>
 * <li>A multiple value property referencing another persisted entity through a DAO</li>
 * </ul>
 *
 * @param <T> type of the entity owning the  property.
 */
public interface Property<T extends HasOid> {
    /**
     * Returns the name of the property.
     *
     * @return name of the property
     */
    String name();

    /**
     * Returns the type of the entity owning the property.
     *
     * @return type of the declaring entity
     */
    Class<T> entity();

    /**
     * Returns true if the type of the format of the property is a managed persisted type, otherwise false.
     *
     * @return true if type of the property is managed
     */
    boolean hasManagedType();

    /**
     * Returns true if the property contains multiple instances as in a collection, otherwise false.
     *
     * @return true if the property has multiple values
     */
    boolean hasMultipleValues();

}
