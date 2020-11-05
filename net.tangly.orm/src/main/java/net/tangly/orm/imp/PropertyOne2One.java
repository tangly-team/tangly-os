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

package net.tangly.orm.imp;

import java.security.PrivilegedActionException;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;

import net.tangly.core.HasOid;
import net.tangly.commons.lang.Reference;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.orm.Dao;
import net.tangly.orm.Property;
import net.tangly.orm.Relation;
import org.jetbrains.annotations.NotNull;


/**
 * Models a property with at most one managed instance of a target entity. The following rules apply
 * <ul>
 *     <li>The name of the property identifies the field in the instance owning the property.</li>
 *     <li>THe name of the property is a column in the owning entity table with a BIGINT sql type.</li>
 * </ul>
 *
 * @param <T> class owning the property
 * @param <R> Class referenced by the property
 */
public class PropertyOne2One<T extends HasOid, R extends HasOid> extends PropertySimple<T> implements Relation<T, R> {
    /**
     * The DAO responsible for the owned object in the 1 - 1 relation.
     */
    private final Reference<Dao<R>> type;
    private final boolean owned;

    public PropertyOne2One(@NotNull String name, @NotNull Class<T> entity, @NotNull Reference<Dao<R>> type, boolean owned) {
        super(name, entity, Long.class, Types.BIGINT,
                Map.of(Property.ConverterType.java2jdbc, (HasOid o) -> Objects.nonNull(o) ? o.oid() : null, Property.ConverterType.jdbc2java,
                        (Long o) -> Objects.nonNull(o) ? type.reference().find(o).orElse(null) : null));
        this.type = type;
        this.owned = owned;
    }

    @Override
    public Dao<R> type() {
        return type.reference();
    }

    @Override
    public boolean isOwned() {
        return owned;
    }

    @Override
    public void update(@NotNull T entity) throws PrivilegedActionException {
        R ownee = (R) ReflectionUtilities.get(entity, field);
        if (Objects.nonNull(ownee)) {
            type().update(ownee);
        }
    }

    @Override
    public void retrieve(@NotNull T entity, Long fid) throws PrivilegedActionException {
        // the property is already been updated through the simple property retrieve mechanism.
    }

    @Override
    public void delete(@NotNull T entity) throws PrivilegedActionException {
        if (isOwned()) {
            R ownee = (R) ReflectionUtilities.get(entity, field);
            if (Objects.nonNull(ownee)) {
                type().delete(ownee);
            }
        }
    }
}
