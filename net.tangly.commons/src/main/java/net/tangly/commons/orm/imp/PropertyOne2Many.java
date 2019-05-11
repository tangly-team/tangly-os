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

import net.tangly.commons.orm.Property;
import net.tangly.commons.orm.Dao;
import net.tangly.commons.models.HasOid;
import net.tangly.commons.utilities.ReflectionUtilities;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Models a property with managed objects as values and supporting multiple objects. When the property is read we retrieve all instances which
 * foreign key format is equal to the unique object identifier oid of the owner of the property. When writing the foreign key property with the
 * unique object identifier oid of the owner of the property and when persist all the objects.
 *
 * @param <T> class owning the property
 * @param <R> Class referenced by the property
 */
public class PropertyOne2Many<T extends HasOid, R extends HasOid> implements Property<T> {
    private String name;
    private Class<T> entity;
    private String property;
    private Dao<R> type;
    private Field field;

    public PropertyOne2Many(String name, Class<T> entity, String property, Dao<R> type) {
        this.name = name;
        this.entity = entity;
        this.property = property;
        this.type = type;
        field = ReflectionUtilities.findField(entity, name).orElseThrow();
        field.setAccessible(true);
    }

    public Dao<R> type() {
        return type;
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
    public boolean hasManagedType() {
        return true;
    }

    @Override
    public boolean hasMultipleValues() {
        return true;
    }

    public void update(@NotNull T entity) throws IllegalAccessException {
        AbstractProperty foreignProperty = type.getPropertyBy(property).orElseThrow();
        List<R> oldReferences = new ArrayList<>(type.find(property + "=" + entity.oid()));
        for (R reference : oldReferences) {
            foreignProperty.setField(reference, null);
        }
        List<R> newReferences = (List<R>) field.get(entity);
        for (R reference : newReferences) {
            foreignProperty.setField(reference, entity.oid());
        }
        Set<R> references = new HashSet<>(oldReferences);
        references.addAll(newReferences);
        for (R reference : references) {
            type.update(reference);
        }
    }

    public void retrieve(@NotNull T entity) throws IllegalAccessException {
        List<R> references = new ArrayList<>(type.find(property + "=" + entity.oid()));
        Collection<R> collection = (Collection<R>) field.get(entity);
        collection.clear();
        collection.addAll(references);
    }
}
