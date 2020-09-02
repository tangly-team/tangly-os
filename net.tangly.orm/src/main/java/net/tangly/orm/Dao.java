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

package net.tangly.orm;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import net.tangly.bus.core.HasOid;
import net.tangly.bus.providers.InstanceProvider;
import net.tangly.commons.lang.ReflectionUtilities;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Represents the mapping between a Java class and a relational database table containing the values of the instances of the class. The design promotes
 * convention over configuration. The primary key for an entity is always the first field with the name oid and of type long.
 *
 * @param <T> the class being mapped. The table takes care of the handling of the unique object identifier.
 */
public class Dao<T extends HasOid> implements InstanceProvider<T> {
    private static final String PRIMARY_KEY = "oid";
    private static final int KEY_SQL_TYPE = Types.BIGINT;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Dao.class);
    private static final AtomicLong oidGenerator = new AtomicLong(0);

    private final String schema;
    private final String entityName;
    private final Class<T> type;
    private final DataSource dataSource;
    private final Constructor<T> constructor;
    private final List<Property<T>> properties;
    private final List<Relation<T, ?>> one2one;
    private final List<Relation<T, ?>> one2many;
    private final String findSql;
    private final String replaceSql;
    private final String deleteSql;
    private final String findWhereSql;

    /**
     * Cache holding all loaded instances handled through the record.
     */
    private final Map<Long, WeakReference<T>> cache;

    public Dao(String schema, @NotNull String entity, @NotNull Class<T> type, @NotNull DataSource dataSource, @NotNull List<Property<T>> properties,
               @NotNull List<Relation<T, ?>> one2one, @NotNull List<Relation<T, ?>> one2many) throws NoSuchMethodException {
        this.schema = schema;
        this.entityName = entity;
        this.type = type;
        this.dataSource = dataSource;
        this.cache = new HashMap<>();
        this.properties = List.copyOf(properties);
        this.one2one = List.copyOf(one2one);
        this.one2many = List.copyOf(one2many);
        constructor = type.getConstructor();
        findSql = generateFindSql();
        replaceSql = generateReplaceSql();
        deleteSql = generateDeleteSql();
        findWhereSql = generateFindWhereSql();
    }

    public Class<T> type() {
        return type;
    }

    /**
     * Returns the property with the given name.
     *
     * @param name name of the property to be found
     * @return optional found property
     */
    public Optional<Property<T>> getPropertyBy(@NotNull String name) {
        return properties.stream().filter(o -> o.name().equals(name)).findAny();
    }

    // region of-CRUD and instance provider

    @Override
    public Optional<T> find(long oid) {
        Optional<T> entity = retrieveFromCache(oid);
        if (entity.isEmpty()) {
            try (var connection = dataSource.getConnection(); var stmt = connection.prepareStatement(findSql)) {
                stmt.setObject(1, oid, KEY_SQL_TYPE);
                try (ResultSet set = stmt.executeQuery()) {
                    if (set.next()) {
                        entity = Optional.of(materializeEntity(set));
                    }
                }
            } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException | PrivilegedActionException e) {
                logger.atError().log("Exception occurred when retrieving entity {} id {}", entityName, oid, e);
            }
        }
        return entity;
    }

    @Override
    public List<T> items() {
        return find("TRUE");
    }

    /**
     * Updates the persistent data associated with the entity.If the entity is new a row is inserted into the table otherwise the columns are updated. The
     * update is transitive and all referenced entities are also updated.
     *
     * @param entity entity to update
     */
    @Override
    public void update(@NotNull T entity) {
        try (var connection = dataSource.getConnection(); var stmt = connection.prepareStatement(replaceSql)) {
            if (entity.oid() == HasOid.UNDEFINED_OID) {
                ReflectionUtilities.set(entity, properties.get(0).field(), oidGenerator.incrementAndGet());
            }
            for (int i = 0; i < properties.size(); i++) {
                properties.get(i).setParameter(stmt, i + 1, entity);
            }
            for (var relation : one2one) {
                relation.update(entity);
            }
            stmt.executeUpdate();
            for (var relation : one2many) {
                relation.update(entity);
            }
            addToCache(entity);
        } catch (SQLException | PrivilegedActionException e) {
            logger.atError().log("Exception creating {} id {}", entityName, entity.oid(), e);
        }
    }

    public void delete(@NotNull T entity) {
        try (var connection = dataSource.getConnection(); var stmt = connection.prepareStatement(deleteSql)) {
            for (var relation : one2many) {
                relation.delete(entity);
            }
            for (var relation : one2one) {
                relation.delete(entity);
            }
            stmt.setObject(1, entity.oid(), KEY_SQL_TYPE);
            stmt.executeUpdate();
            removeFromCache(entity.oid());
        } catch (SQLException | PrivilegedActionException e) {
            logger.atError().log("Error when deleting instance {} id {}", entityName, entity.oid(), e);
        }
    }

    public List<T> find(String where) {
        List<T> entities = new ArrayList<>();
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement(); var set = stmt.executeQuery(findWhereSql + where)) {
            while (set.next()) {
                Optional<T> instance = retrieveFromCache((Long) set.getObject(1));
                entities.add(instance.isPresent() ? instance.get() : materializeEntity(set));
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException | PrivilegedActionException e) {
            logger.atError().log("Exception occurred when retrieving entity with id {}", entityName, e);
        }
        return entities;
    }

    /**
     * Retrieves the values of the properties from the result set and retrieve the values of the relations.
     *
     * @param set result set containing the field values
     * @return the new instance with filled properties and relations
     * @throws SQLException if a problem was encountered when retrieving the field values from the result set
     */
    private T materializeEntity(@NotNull ResultSet set) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException,
            PrivilegedActionException {
        T entity = constructor.newInstance();
        for (int i = 0; i < properties.size(); i++) {
            properties.get(i).getParameter(set, i + 1, entity);
        }
        for (var relation : one2many) {
            relation.retrieve(entity, entity.oid());
        }
        addToCache(entity);
        return entity;
    }

    public void delete(long oid) {
        find(oid).ifPresent(this::delete);
    }

    // endregion

    // region cache operations

    public void clearCache() {
        cache.clear();
    }

    private Optional<T> retrieveFromCache(long id) {
        if (cache.containsKey(id)) {
            WeakReference<T> reference = cache.get(id);
            T entity = reference.get();
            if (entity != null) {
                return Optional.of(entity);
            } else {
                cache.remove(id);
            }
        }
        return Optional.empty();
    }

    private void addToCache(@NotNull T entity) {
        if (cache.containsKey(entity.oid())) {
            logger.atDebug().log("Invalidate cache {} for id {}", getClass().getSimpleName(), entity.oid());
        } else {
            logger.atDebug().log("Add to cache {} id {}", getClass().getSimpleName(), entity.oid());
        }
        cache.put(entity.oid(), new WeakReference<>(entity));
    }

    private void removeFromCache(long id) {
        if (cache.containsKey(id)) {
            logger.atDebug().log("Invalidate cache {} for id {}", getClass().getSimpleName(), id);
            cache.remove(id);
        }
    }

    // endregion

    // region SQL statements

    private String generateReplaceSql() {
        return "REPLACE INTO " + tableName() + " (" + properties.stream().map(Property::name).collect(Collectors.joining(", ")) + ") VALUES (" + "?" +
                String.join("", Collections.nCopies(properties.size() - 1, ", ?")) + ")";
    }

    private String generateDeleteSql() {
        return "DELETE FROM " + tableName() + " WHERE " + PRIMARY_KEY + "=?";
    }

    private String generateFindSql() {
        return "SELECT " + properties.stream().map(Property::name).collect(Collectors.joining(", ")) + " FROM " + tableName() + " WHERE " + PRIMARY_KEY +
                " = ?";
    }

    private String generateFindWhereSql() {
        return "SELECT " + properties.stream().map(Property::name).collect(Collectors.joining(", ")) + " FROM " + tableName() + " WHERE ";
    }

    private String tableName() {
        return ((schema != null) ? (schema + "." + entityName) : entityName);
    }

    // endregion
}
