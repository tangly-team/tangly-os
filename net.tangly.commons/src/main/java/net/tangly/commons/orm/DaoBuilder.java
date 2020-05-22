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

package net.tangly.commons.orm;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.sql.DataSource;

import net.tangly.bus.codes.Code;
import net.tangly.bus.codes.CodeType;
import net.tangly.bus.core.HasOid;
import net.tangly.bus.core.Tag;
import net.tangly.commons.lang.Reference;
import net.tangly.commons.orm.imp.PropertyCode;
import net.tangly.commons.orm.imp.PropertyJson;
import net.tangly.commons.orm.imp.PropertyOne2Many;
import net.tangly.commons.orm.imp.PropertyOne2One;
import net.tangly.commons.orm.imp.PropertySimple;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for the table class. Upon building the class you should discard the builder instance. Any additional call on the builder will update a
 * runtime exception.
 */
public class DaoBuilder<T extends HasOid> {
    private static final Map<Property.ConverterType, Function<?, ?>> ID_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java, (Function<String, Object>) Long::valueOf);
    private static final Map<Property.ConverterType, Function<?, ?>> TEXT_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, UnaryOperator.identity(), Property.ConverterType.text2java, UnaryOperator.identity());
    private static final Map<Property.ConverterType, Function<?, ?>> INTEGER_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java,
                    (Function<String, Object>) Integer::valueOf);
    private static final Map<Property.ConverterType, Function<?, ?>> BIGDECIMAL_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java, (Function<String, Object>) BigDecimal::new);
    private static final Map<Property.ConverterType, Function<?, ?>> LOCALDATE_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java,
                    (Function<String, Object>) LocalDate::parse);
    private static final Map<Property.ConverterType, Function<?, ?>> LOCALDATETIME_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java,
                    (Function<String, Object>) LocalDateTime::parse);
    private static final Map<Property.ConverterType, Function<?, ?>> TAGS_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, (Function<Set<Tag>, Object>) Tag::text, Property.ConverterType.jdbc2java,
                    (Function<String, Object>) Tag::toTags, Property.ConverterType.java2text, (Function<Set<Tag>, Object>) Tag::text,
                    Property.ConverterType.text2java, (Function<String, Object>) Tag::toTags);

    private final Class<T> type;
    private final List<Property<T>> properties;
    private final List<PropertyOne2Many<T, ?>> relations;
    private final Reference<Dao<T>> self = Reference.empty();


    public DaoBuilder(@NotNull Class<T> type) {
        this.type = type;
        this.properties = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    public DaoBuilder<T> withOid() {
        properties.add(new PropertySimple<>("oid", type, Long.TYPE, Types.BIGINT, ID_CONVERTER));
        return this;
    }

    public DaoBuilder<T> withFid(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, Long.TYPE, Types.BIGINT, ID_CONVERTER));
        return this;
    }

    /**
     * Add an integer property and column.
     *
     * @param name name of the property and associated column in the table
     * @return builder as fluent interface
     */
    public DaoBuilder<T> withInt(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, Integer.class, Types.INTEGER, INTEGER_CONVERTER));
        return this;
    }

    public DaoBuilder<T> withLong(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, Long.class, Types.BIGINT, ID_CONVERTER));
        return this;
    }

    public DaoBuilder<T> withString(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, String.class, Types.VARCHAR, TEXT_CONVERTER));
        return this;
    }

    public DaoBuilder<T> withText(@NotNull String name) {
        return (withString(name));
    }

    public DaoBuilder<T> withDate(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, LocalDate.class, Types.DATE, LOCALDATE_CONVERTER));
        return this;
    }

    public DaoBuilder<T> withDateTime(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, LocalDateTime.class, Types.TIMESTAMP, LOCALDATETIME_CONVERTER));
        return this;
    }

    public DaoBuilder<T> withBigDecimal(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, BigDecimal.class, Types.DECIMAL, BIGDECIMAL_CONVERTER));
        return this;
    }

    public DaoBuilder<T> withTags(@NotNull String name) {
        properties.add(new PropertySimple<>(name, type, String.class, Types.VARCHAR, TAGS_CONVERTER));
        return this;
    }

    public <U> DaoBuilder<T> withJson(@NotNull String name, @NotNull Class<U> referenceType) {
        properties.add(new PropertyJson<>(name, type, referenceType));
        return this;
    }

    public <U extends Code> DaoBuilder<T> withCode(@NotNull String name, @NotNull CodeType<U> codeType) {
        properties.add(new PropertyCode<>(name, type, codeType));
        return this;
    }

    public DaoBuilder<T> withOne2One(@NotNull String name) {
        properties.add(new PropertyOne2One<>(name, type, self));
        return this;
    }

    public <R extends HasOid> DaoBuilder<T> withOne2One(@NotNull String name, @NotNull Reference<Dao<R>> reference) {
        properties.add(new PropertyOne2One<>(name, type, reference));
        return this;
    }

    public DaoBuilder<T> withOne2Many(@NotNull String name, @NotNull String property) {
        relations.add(new PropertyOne2Many<>(name, type, property, self));
        return this;
    }

    public <R extends HasOid> DaoBuilder<T> withOne2Many(String name, String property, Reference<Dao<R>> reference) {
        relations.add(new PropertyOne2Many<>(name, type, property, reference));
        return this;
    }

    public Dao<T> build(String schema, String entity, DataSource dataSource) throws NoSuchMethodException {
        self.reference(new Dao<>(schema, entity, type, dataSource, properties, relations));
        return self.reference();
    }
}
