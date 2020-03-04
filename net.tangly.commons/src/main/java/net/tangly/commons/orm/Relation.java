/*
 * Copyright 2006-2020 Marcel Baumann
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

import net.tangly.bus.core.HasOid;

/**
 * Models a directional relation between two classes
 *
 * @param <T> Class of the owner of the relation, meaning the start of the directional relation
 * @param <R> Class of the ownee of the relation, meaning the end of the directional relation
 */
public interface Relation<T extends HasOid, R extends HasOid> {
    /**
     * Returns the DAO is charge of the objects at the end of the relation.
     *
     * @return the DAO of the objects at the end of the relation
     */
    Dao<R> type();
}
