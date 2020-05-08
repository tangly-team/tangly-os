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

package net.tangly.commons.orm.imp;

import net.tangly.bus.core.HasOid;
import net.tangly.commons.lang.Reference;
import net.tangly.commons.orm.Dao;
import net.tangly.commons.orm.Property;
import net.tangly.commons.orm.Relation;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;


/**
 * Models a property with managed objects as values and supporting one object.
 *
 * @param <T> class owning the property
 * @param <R> Class referenced by the property
 */
public class PropertyOne2One<T extends HasOid, R extends HasOid> extends PropertySimple<T> implements Relation<T, R> {
    /**
     * The DAO responsible for the owned object in the 1 - 1 relation.
     */
    private Reference<Dao<R>> type;

    public PropertyOne2One(@NotNull String name, @NotNull Class<T> entity, @NotNull Reference<Dao<R>> type) {
        super(name, entity, Long.class, Types.BIGINT,
                Map.of(Property.ConverterType.java2jdbc, (HasOid o) -> Objects.nonNull(o) ? o.oid() : null, Property.ConverterType.jdbc2java,
                        (Long o) -> Objects.nonNull(o) ? type.reference().find(o).orElse(null) : null, Property.ConverterType.java2text,
                        (HasOid o) -> Objects.nonNull(o) ? Long.toString(o.oid()) : null, Property.ConverterType.text2java,
                        (String o) -> Objects.nonNull(o) ? type.reference().find(Long.parseLong(o)).orElse(null) : null));
        this.type = type;
    }

    @Override
    public Dao<R> type() {
        return type.reference();
    }

    @Override
    public void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        R ownee = (R) field().get(entity);
        if (Objects.nonNull(ownee)) {
            type().update(ownee);
        }
        Object value = get(entity, ConverterType.java2jdbc);
        if (value != null) {
            statement.setObject(index, value, sqlType());
        } else {
            statement.setNull(index, sqlType());
        }
    }
}
