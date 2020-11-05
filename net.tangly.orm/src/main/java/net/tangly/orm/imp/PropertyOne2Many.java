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

import java.lang.reflect.Field;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import net.tangly.core.HasOid;
import net.tangly.commons.lang.Reference;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.orm.Dao;
import net.tangly.orm.Property;
import net.tangly.orm.Relation;
import org.jetbrains.annotations.NotNull;

/**
 * Models a propertyBy with managed objects as values and supporting multiple objects. When the propertyBy is read we retrieve all instances which foreign key
 * format is equal to the unique object identifier oid of the owner of the propertyBy. When writing the foreign key propertyBy with the unique object identifier
 * oid of the owner of the propertyBy and when persist all the objects.
 *
 * @param <T> class owning the propertyBy
 * @param <R> Class referenced by the propertyBy
 */
public class PropertyOne2Many<T extends HasOid, R extends HasOid> implements Property<T>, Relation<T, R> {
    private final String name;
    private final Class<T> entity;
    private final String propertyBy;
    /**
     * The DAO responsible for the owned object in the 1 - N relation.
     */
    private final Reference<Dao<R>> type;
    private final boolean owned;
    private final Field field;

    public PropertyOne2Many(@NotNull String name, @NotNull Class<T> entity, @NotNull String property, @NotNull Reference<Dao<R>> type, boolean owned) {
        this.name = name;
        this.entity = entity;
        this.propertyBy = property;
        this.type = type;
        this.owned = owned;
        field = ReflectionUtilities.findField(entity, name).orElseThrow();
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

    @Override
    public boolean isOwned() {
        return owned;
    }

    @Override
    public void update(@NotNull T entity) throws PrivilegedActionException {
        Property foreignProperty = type().getPropertyBy(propertyBy).orElseThrow();
        List<R> oldReferences = new ArrayList<>(type().find(propertyBy + "=" + entity.oid()));
        List<R> newReferences = (List<R>) ReflectionUtilities.get(entity, field);
        oldReferences.removeAll(newReferences);
        for (R removedItem : oldReferences) {
            if (isOwned()) {
                type().delete(removedItem);
            } else {
                updateForeignKey(entity, removedItem, foreignProperty, null);
            }
        }
        for (R currentItem : newReferences) {
            updateForeignKey(entity, currentItem, foreignProperty, entity.oid());
        }
    }

    @Override
    public void retrieve(@NotNull T entity, Long fid) throws PrivilegedActionException {
        Collection<R> collection = (Collection<R>) ReflectionUtilities.get(entity, field);
        collection.clear();
        collection.addAll(type().find(propertyBy + "=" + entity.oid()));
    }

    @Override
    public void delete(@NotNull T entity) throws PrivilegedActionException {
        if (isOwned()) {
            ((List<R>) ReflectionUtilities.get(entity, field)).forEach(t -> type().delete(t));
        }
    }

    private void updateForeignKey(@NotNull T entity, @NotNull R referencedEntity, @NotNull Property foreignProperty, Object foreignKey) throws
            PrivilegedActionException {
        Long oldForeignKey = (Long) ReflectionUtilities.get(referencedEntity, foreignProperty.field());
        if (!Objects.equals(oldForeignKey, foreignKey)) {
            ReflectionUtilities.set(referencedEntity, foreignProperty.field(), foreignKey);
            type().update(referencedEntity);
        }
    }
}
