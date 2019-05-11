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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import net.tangly.commons.models.HasOid;
import net.tangly.commons.utilities.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

/**
 * A property which stores its values as as JSON representation in one column in the database table. Multi-format properties are supported.
 *
 * @param <T> type of the owner of the property
 * @param <V> type of the format of the single or multiple values property
 */
public class PropertyJson<T extends HasOid, V> extends AbstractProperty<T> {
    private static ObjectMapper mapper = createObjectMapper();
    private boolean hasMultipleValues;
    private Class<V> type;

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
        return mapper;
    }

    /**
     * Constructor of the JSON property.
     *
     * @param name              name of the property
     * @param entity            class owning the mapped property
     * @param referenceType     type of the content of the property. If the property is multiple, indicates the class of a single item
     * @param hasMultipleValues indicates if the property has multiple values or just a single one
     */
    public PropertyJson(String name, Class<T> entity, Class<V> referenceType, boolean hasMultipleValues) {
        super(name, entity);
        this.type = referenceType;
        this.hasMultipleValues = hasMultipleValues;
    }

    @Override
    public boolean hasMultipleValues() {
        return hasMultipleValues;
    }

    @Override
    public void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException,
            JsonProcessingException {
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String json = writer.writeValueAsString(field.get(entity));
        if (Strings.isNullOrEmpty(json)) {
            statement.setNull(index, Types.CLOB);
        } else {
            statement.setObject(index, json, Types.CLOB);
        }
    }

    @Override
    public void setField(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException, IOException {
        String json = set.getObject(index, String.class);
        if (json != null) {
            JavaType itemType = mapper.getTypeFactory().constructCollectionType(List.class, type);
            List<V> items = mapper.readValue(json, itemType);
            Collection<V> collection = (Collection<V>) field.get(entity);
            collection.clear();
            collection.addAll(items);
        }
    }
}
