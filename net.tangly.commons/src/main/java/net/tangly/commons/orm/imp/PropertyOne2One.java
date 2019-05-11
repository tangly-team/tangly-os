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

package net.tangly.commons.orm.imp;

import net.tangly.commons.orm.Dao;
import net.tangly.commons.models.HasOid;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


/**
 * Models a property with managed objects as values and supporting one object.
 *
 * @param <T> class owning the property
 * @param <R> Class referenced by the property
 */
public class PropertyOne2One<T extends HasOid, R extends HasOid> extends AbstractProperty<T> {
    private Dao<R> type;

    public PropertyOne2One(String name, Class<T> clazz, Dao<R> type) {
        super(name, clazz);
        this.type = type;
    }

    public Dao<R> type() {
        return type;
    }

    @Override
    public boolean hasManagedType() {
        return true;
    }

    @Override
    public void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        R reference = (R) field.get(entity);
        if (reference != null) {
            type.update(reference);
            statement.setObject(index, reference.oid(), Types.BIGINT);
        } else {
            statement.setNull(index, Types.BIGINT);
        }
    }

    @Override
    public void setField(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        Long referenceOid = set.getObject(index, Long.TYPE);
        Object reference = (referenceOid == null) ? null : type.find(referenceOid).orElse(null);
        field.set(entity, reference);
    }


}
