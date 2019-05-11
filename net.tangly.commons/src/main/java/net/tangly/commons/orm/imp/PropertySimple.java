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

import net.tangly.commons.models.HasOid;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Models a property with simple objects. The cardinality can be one or multiple
 *
 * @param <T> class owning the property
 */
public class PropertySimple<T extends HasOid> extends AbstractProperty<T> {
    private Class<?> sqlClass;
    private int sqlType;
    private Function<Object, Object> java2jdbc;
    private Function<Object, Object> jdbc2java;

    public PropertySimple(String name, Class<T> clazz, Class<?> sqlClass, int sqlType) {
        this(name, clazz, sqlClass, sqlType, UnaryOperator.identity(), UnaryOperator.identity());
    }

    public PropertySimple(String name, Class<T> clazz, Class<?> sqlClass, int sqlType, UnaryOperator<Object> java2jdbc,
                          UnaryOperator<Object> jdbc2Java) {
        super(name, clazz);
        this.sqlClass = sqlClass;
        this.sqlType = sqlType;
        this.java2jdbc = java2jdbc;
        this.jdbc2java = jdbc2Java;
    }

    @Override
    public void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        Object value = java2jdbc.apply(field.get(entity));
        if (value != null) {
            statement.setObject(index, value, sqlType);
        } else {
            statement.setNull(index, sqlType);
        }
    }

    @Override
    public void setField(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        Object value = jdbc2java.apply(set.getObject(index, sqlClass));
        field.set(entity, value);
    }
}
