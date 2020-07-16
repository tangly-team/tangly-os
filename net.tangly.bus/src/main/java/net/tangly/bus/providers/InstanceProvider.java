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

package net.tangly.bus.providers;

import java.util.Optional;

import net.tangly.bus.core.HasOid;

/**
 * Defines the abstraction to handle all instances of a business model abstraction type.
 *
 * @param <T> type of the business model abstraction
 */
public interface InstanceProvider<T extends HasOid> extends Provider<T> {
    /**
     * Find the entity instance with the given unique object identifier.
     *
     * @param oid object identifier of the instance to find
     * @return optional of the requested entity
     */
    Optional<T> find(long oid);
}
