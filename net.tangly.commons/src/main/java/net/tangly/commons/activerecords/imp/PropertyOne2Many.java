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

package net.tangly.commons.activerecords.imp;

import net.tangly.commons.activerecords.Property;
import net.tangly.commons.activerecords.Table;
import net.tangly.commons.models.HasOid;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a property with managed objects as values and supporting multiple objects. When the property is read we retrieve all instances which
 * foreign key value is equal to the unique object identifier oid of the owner of the property. When writing the foreign key property with the
 * unique object identifier oid of the owner of the property and when persist all the objects.
 *
 * @param <T> class owning the property
 * @param <R> Class referenced by the property
 */
public class PropertyOne2Many<T extends HasOid, R extends HasOid> extends Property<T> {
    private Table<R> type;
    private String property;

    public PropertyOne2Many(String name, Class<T> clazz, Table<R> type) throws NoSuchFieldException {
        super(name, clazz);
        this.type = type;
    }

    public Table<R> type() {
        return type;
    }

    @Override
    public boolean hasPersistedType() {
        return true;
    }

    @Override
    public void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        List<R> references = (List<R>) field.get(entity);
        Property foreignProperty = type.getPropertyBy(property).orElse(null);
        for (R reference : references) {
            foreignProperty.setField(reference, entity.oid());
            type.update(reference);
        }

    }

    @Override
    public void setField(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        List<R> references = new ArrayList<>(type.find(property + "=" + entity.oid()));
        field.set(entity, references);

    }
}
