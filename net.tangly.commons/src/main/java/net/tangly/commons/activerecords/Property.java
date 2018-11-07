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

package net.tangly.commons.activerecords;

import net.tangly.commons.models.HasOid;
import net.tangly.commons.models.Tag;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Models a property of a class which is persisted in a relational database.
 *
 * @param <T> class owning the property
 */
public abstract class Property<T extends HasOid> {
    static Object tags_java2jdbc(Object object) {
        return Tag.toString((Set<Tag>) object);
    }

    static Object tags_jdbc2java(Object object) {
        return Tag.toTags((String) object);
    }

    /**
     * Name of the property.
     */
    protected String name;
    protected Class<T> clazz;
    protected Field field;

    public Property(String name, Class<T> clazz) throws NoSuchFieldException {
        this.name = name;
        this.clazz = clazz;
        field = findField(name);
        field.setAccessible(true);
    }

    public String name() {
        return name;
    }

    public boolean hasPersistedType() {
        return false;
    }

    public abstract void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException;

    public abstract void setField(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException;

    public void setField(@NotNull T entity, Object value) throws IllegalAccessException {
        field.set(entity, value);
    }

    private Field findField(@NotNull String name) {
        Field field = null;
        Class<?> pointer = clazz;
        while ((pointer != null) && (field == null)) {
            Field[] fields = pointer.getDeclaredFields();
            Optional<Field> result = Arrays.stream(fields).filter(o -> name.equals(o.getName())).findAny();
            if (result.isPresent()) {
                field = result.get();
            }
            pointer = pointer.getSuperclass();
        }
        return field;
    }
}
