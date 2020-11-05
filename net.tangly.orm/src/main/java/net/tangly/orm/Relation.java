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

package net.tangly.orm;

import java.security.PrivilegedActionException;

import net.tangly.core.HasOid;
import org.jetbrains.annotations.NotNull;

/**
 * Models a directional relation between two classes. The following aspects are tracked.
 * <ul>
 *     <li>The DAO responsible to handle the instances at the end of the relation.</li>
 *     <li>Updates are always handled transitively. All instances referenced directly and indirectly through relations are update each time an
 *     instance is updated.</li>
 *     <li>The fact if the instances at the end of the relation are exclusively owned and therefore updates and deletions shall be handled
 *     transitively. If not they shared are shared between objects and therefore the application is in charge of deletions.</li>
 * </ul>
 *
 * @param <T> Class of the owner of the relation, meaning the start of the directional relation
 * @param <R> Class of the ownee of the relation, meaning the end of the directional relation
 */
public interface Relation<T extends HasOid, R extends HasOid> {
    /**
     * Returns the DAO is charge of the objects at the end of the relation. Therefore only typed relations are supported.
     *
     * @return the DAO of the objects at the end of the relation
     */
    Dao<R> type();

    /**
     * Returns flag indicating if the instances at the end of the relation are owned or not.
     *
     * @return value of the flag
     */
    boolean isOwned();

    /**
     * Updates all the instances at the end of the relation.
     *
     * @param entity entity owning the relation
     * @throws PrivilegedActionException if an error occurred when accessing field for foreign key or instance
     */
    void update(@NotNull T entity) throws PrivilegedActionException;

    /**
     * Retrieves all the instances at the end of the relation.
     *
     * @param entity entity owning the relation
     * @param fid    foreign key of the referenced entities. Currently in a {@code N -> 1} relation the fid is the oid of the referenced object, in a
     *               {@code 1 -> N} the fid is the oid of entity
     * @throws PrivilegedActionException if an error occurred when accessing field for foreign key or instance
     */
    void retrieve(@NotNull T entity, Long fid) throws PrivilegedActionException;

    /**
     * Deletes all the instances at the end of the relation. Semantically this method performs only if {@link #isOwned()} returns true.
     *
     * @param entity entity owning the relation
     * @throws PrivilegedActionException if an error occurred when accessing field for foreign key or instance
     */
    void delete(@NotNull T entity) throws PrivilegedActionException;
}
