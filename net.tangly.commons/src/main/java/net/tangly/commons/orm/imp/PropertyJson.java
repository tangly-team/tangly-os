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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import net.tangly.bus.core.HasOid;
import net.tangly.commons.orm.Property;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A property which stores its values as as JSON representation in one column in the database table. Multi-format properties are supported.
 *
 * @param <T> type of the owner of the property
 * @param <V> type of the format of the single or multiple values property
 */
public class PropertyJson<T extends HasOid, V> extends PropertySimple<T> {
    private static ObjectMapper mapper = createObjectMapper();

    private static String toJson(Object value) {
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            return writer.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <V> List<V> fromJson(String text, Class<V> referenceType) {
        JavaType itemType = mapper.getTypeFactory().constructCollectionType(List.class, referenceType);
        try {
            List<V> items = mapper.readValue(text, itemType);
            return items;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
        return mapper;
    }

    private Class<V> referenceType;

    /**
     * Constructor of the JSON property.
     *
     * @param name   name of the property
     * @param entity class owning the mapped property
     * @param referenceType class of the reference type to map
     */
    public PropertyJson(String name, Class<T> entity, final Class<V> referenceType) {
        super(name, entity, String.class, Types.VARCHAR,
                Map.of(Property.ConverterType.java2jdbc, PropertyJson::toJson, Property.ConverterType.jdbc2java,
                        (String o) -> PropertyJson.fromJson(o, referenceType), Property.ConverterType.java2text, PropertyJson::toJson,
                        Property.ConverterType.text2java, (String o) -> PropertyJson.fromJson(o, referenceType)));
        this.referenceType = referenceType;
    }

    @Override
    public <P, V> void set(@NotNull T entity, V value, @NotNull ConverterType type) throws IllegalAccessException, IllegalArgumentException {
        Function<V, P> convert = getConverter(type);
        List<V> items = (List<V>) fromJson((String) value, referenceType);
        Collection<V> collection = (Collection<V>) field().get(entity);
        collection.clear();
        collection.addAll(items);
    }
}
