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

package net.tangly.commons.orm.imp;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import net.tangly.bus.core.HasOid;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.commons.orm.Property;
import org.jetbrains.annotations.NotNull;

/**
 * Models a property with simple Java objects or primitive. The type of the property is not defined through a DAO.
 *
 * @param <T> class owning the property
 */
public class PropertySimple<T extends HasOid> implements Property<T> {
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

    private final Class<?> jdbcType;
    private final int sqlType;

    protected EnumMap<ConverterType, Function<?, ?>> converters;


    public PropertySimple(@NotNull String name, @NotNull Class<T> entity, @NotNull Class<?> jdbcType, int sqlType,
                          @NotNull Map<ConverterType, Function<?, ?>> converters) {
        this.name = name;
        this.entity = entity;
        this.jdbcType = jdbcType;
        this.sqlType = sqlType;
        this.converters = new EnumMap<>(converters);
        field = ReflectionUtilities.findField(entity, name).orElseThrow();
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
    public Field field() {
        return field;
    }

    @Override
    public int sqlType() {
        return sqlType;
    }

    @Override
    public Class<?> jdbcType() {
        return jdbcType;
    }


    @Override
    public <T, R> Function<T, R> getConverter(@NotNull ConverterType type) {
        return (Function<T, R>) converters.get(type);
    }
}


