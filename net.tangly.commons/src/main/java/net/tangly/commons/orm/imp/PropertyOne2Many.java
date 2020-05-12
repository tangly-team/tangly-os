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
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.commons.orm.Dao;
import net.tangly.commons.orm.Property;
import net.tangly.commons.orm.Relation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Models a propertyBy with managed objects as values and supporting multiple objects. When the propertyBy is read we retrieve all instances which
 * foreign key format is equal to the unique object identifier oid of the owner of the propertyBy. When writing the foreign key propertyBy with the
 * unique object identifier oid of the owner of the propertyBy and when persist all the objects.
 *
 * @param <T> class owning the propertyBy
 * @param <R> Class referenced by the propertyBy
 */
public class PropertyOne2Many<T extends HasOid, R extends HasOid> implements Property<T>, Relation<T, R> {
    private final String name;
    private final Class<T> entity;
    private final String propertyBy;
    private final Reference<Dao<R>> type;
    private final Field field;

    public PropertyOne2Many(@NotNull String name, @NotNull Class<T> entity, @NotNull String property, @NotNull Reference<Dao<R>> type) {
        this.name = name;
        this.entity = entity;
        this.propertyBy = property;
        this.type = type;
        field = ReflectionUtilities.findField(entity, name).orElseThrow();
        field.setAccessible(true);
    }

    @Override
    public Dao<R> type() {
        return type.reference();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<T> entity() {
        return entity;
    }

    @Override
    public Field field() {
        return field;
    }

    @Override
    public int sqlType() {
        return 0;
    }

    @Override
    public Class<?> jdbcType() {
        return null;
    }

    @Override
    public <T1, R> Function<T1, R> getConverter(@NotNull ConverterType type) {
        return null;
    }

    public void update(@NotNull T entity) throws IllegalAccessException {
        Property foreignProperty = type().getPropertyBy(propertyBy).orElseThrow();
        List<R> oldReferences = new ArrayList<>(type().find(propertyBy + "=" + entity.oid()));
        for (R reference : oldReferences) {
            foreignProperty.field().set(reference, null);
        }
        List<R> newReferences = (List<R>) field.get(entity);
        for (R reference : newReferences) {
            foreignProperty.field().set(reference, entity.oid());
        }
        Set<R> references = new HashSet<>(oldReferences);
        references.addAll(newReferences);
        for (R reference : references) {
            type().update(reference);
        }
    }

    public void retrieve(@NotNull T entity) throws IllegalAccessException {
        List<R> references = new ArrayList<>(type().find(propertyBy + "=" + entity.oid()));
        Collection<R> collection = (Collection<R>) field.get(entity);
        collection.clear();
        collection.addAll(references);
    }
}
