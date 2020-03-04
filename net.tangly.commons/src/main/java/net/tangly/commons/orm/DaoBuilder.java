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

import javax.sql.DataSource;
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

/**
 * Builder for the table class. Upon building the class you should discard the builder instance. Any additional call on the builder will update a
 * runtime exception.
 */
public class DaoBuilder<T extends HasOid> {
    private static Map<Property.ConverterType, Function<?, ?>> ID_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java, (Function<String, Object>) Long::valueOf);
    private static Map<Property.ConverterType, Function<?, ?>> TEXT_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, UnaryOperator.identity(), Property.ConverterType.text2java, UnaryOperator.identity());
    private static Map<Property.ConverterType, Function<?, ?>> INTEGER_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java,
                    (Function<String, Object>) Integer::valueOf);
    private static Map<Property.ConverterType, Function<?, ?>> BIGDECIMAL_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java, (Function<String, Object>) BigDecimal::new);
    private static Map<Property.ConverterType, Function<?, ?>> LOCALDATE_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java,
                    (Function<String, Object>) LocalDate::parse);
    private static Map<Property.ConverterType, Function<?, ?>> LOCALDATETIME_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, UnaryOperator.identity(), Property.ConverterType.jdbc2java, UnaryOperator.identity(),
                    Property.ConverterType.java2text, Object::toString, Property.ConverterType.text2java,
                    (Function<String, Object>) LocalDateTime::parse);
    private static Map<Property.ConverterType, Function<?, ?>> TAGS_CONVERTER =
            Map.of(Property.ConverterType.java2jdbc, (Set<Tag> o) -> Tag.toString(o), Property.ConverterType.jdbc2java,
                    (Function<String, Object>) Tag::toTags, Property.ConverterType.java2text, (Set<Tag> o) -> Tag.toString(o),
                    Property.ConverterType.text2java, (Function<String, Object>) Tag::toTags);

    private Class<T> type;
    private List<Property<T>> properties;
    private List<PropertyOne2Many<T, ?>> relations;
    private Reference<Dao<T>> self = new Reference<>();


    public DaoBuilder(@NotNull Class<T> type) {
        this.type = type;
        this.properties = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    public DaoBuilder withOid() {
        properties.add(new PropertySimple<T>("oid", type, Long.TYPE, Types.BIGINT, ID_CONVERTER));
        return this;
    }

    public DaoBuilder withFid(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, Long.TYPE, Types.BIGINT, ID_CONVERTER));
        return this;
    }

    /**
     * Add an integer property and column.
     *
     * @param name name of the property and associated column in the table
     * @return builder as fluent interface
     */
    public DaoBuilder withInt(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, Integer.class, Types.INTEGER, INTEGER_CONVERTER));
        return this;
    }

    public DaoBuilder withLong(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, Long.class, Types.BIGINT, ID_CONVERTER));
        return this;
    }

    public DaoBuilder withString(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, String.class, Types.VARCHAR, TEXT_CONVERTER));
        return this;
    }

    public DaoBuilder withText(@NotNull String name) {
        return (withString(name));
    }

    public DaoBuilder withDate(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, LocalDate.class, Types.DATE, LOCALDATE_CONVERTER));
        return this;
    }

    public DaoBuilder withDateTime(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, LocalDateTime.class, Types.TIMESTAMP, LOCALDATETIME_CONVERTER));
        return this;
    }

    public DaoBuilder withBigDecimal(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, BigDecimal.class, Types.DECIMAL, BIGDECIMAL_CONVERTER));
        return this;
    }

    public DaoBuilder withTags(@NotNull String name) {
        properties.add(new PropertySimple<T>(name, type, String.class, Types.VARCHAR, TAGS_CONVERTER));
        return this;
    }

    public <U> DaoBuilder withJson(@NotNull String name, @NotNull Class<U> referenceType) {
        properties.add(new PropertyJson<T, U>(name, type, referenceType));
        return this;
    }

    public <U extends Code> DaoBuilder withCode(@NotNull String name, @NotNull CodeType<U> codeType) {
        properties.add(new PropertyCode<T, U>(name, type, codeType));
        return this;
    }

    public DaoBuilder withOne2One(@NotNull String name) {
        properties.add(new PropertyOne2One<T, T>(name, type, self));
        return this;
    }

    public <R extends HasOid> DaoBuilder withOne2One(@NotNull String name, @NotNull Reference<Dao<R>> reference) {
        properties.add(new PropertyOne2One<T, R>(name, type, reference));
        return this;
    }

    public DaoBuilder withOne2Many(@NotNull String name, @NotNull String property) {
        relations.add(new PropertyOne2Many<T, T>(name, type, property, self));
        return this;
    }

    public <R extends HasOid> DaoBuilder withOne2Many(String name, String property, Reference<Dao<R>> reference) {
        relations.add(new PropertyOne2Many<T, R>(name, type, property, reference));
        return this;
    }

    public Dao<T> build(String schema, String entity, DataSource dataSource) throws NoSuchMethodException {
        self.reference(new Dao<T>(schema, entity, type, dataSource, properties, relations));
        return self.reference();
    }
}
