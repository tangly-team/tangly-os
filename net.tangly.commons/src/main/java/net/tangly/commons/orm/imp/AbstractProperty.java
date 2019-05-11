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

import com.fasterxml.jackson.core.JsonProcessingException;
import net.tangly.commons.orm.Property;
import net.tangly.commons.models.HasOid;
import net.tangly.commons.models.Tag;
import net.tangly.commons.utilities.ReflectionUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Models a property of a class which is persisted in a relational database in the same table as other properties of the class.
 *
 * @param <T> class owning the property
 */
public abstract class AbstractProperty<T extends HasOid> implements Property<T> {
    public static Object tags4java2jdbc(Object object) {
        return Tag.toString((Set<Tag>) object);
    }

    public static Object tags4jdbc2java(Object object) {
        return Tag.toTags((String) object);
    }

    /**
     * Name of the property.
     */
    protected final String name;

    /**
     * Class of the owning entity of the property.
     */
    protected final Class<T> entity;

    /**
     * Field associated with the property and used to get and set values of the property.
     */
    protected final Field field;

    public AbstractProperty(String name, Class<T> entity) {
        this.name = name;
        this.entity = entity;
        field = ReflectionUtilities.findField(entity, name).orElseThrow();
        field.setAccessible(true);
    }

    @Override
    public Class<T> entity() {
        return entity;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean hasManagedType() {
        return false;
    }

    @Override
    public boolean hasMultipleValues() {
        return false;
    }


    public abstract void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException,
            JsonProcessingException;

    public abstract void setField(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException, IOException;

    public void setField(@NotNull T entity, Object value) throws IllegalAccessException {
        field.set(entity, value);
    }
}
