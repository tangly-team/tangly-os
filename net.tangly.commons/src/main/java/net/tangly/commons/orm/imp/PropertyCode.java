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

import net.tangly.bus.codes.Code;
import net.tangly.bus.codes.CodeType;
import net.tangly.bus.core.HasOid;
import net.tangly.commons.orm.Property;
import org.jetbrains.annotations.NotNull;

import java.sql.Types;
import java.util.Map;

/**
 * Defines a property type used to store a reference code as type. The mapping to the persistence layer is predifined.
 *
 * @param <T> type of the class owning the property
 * @param <V> type of the reference code stored in the property
 */
public class PropertyCode<T extends HasOid, V extends Code> extends PropertySimple<T> {
    /**
     * The code type describing the set of codes in this property.
     */
    private final CodeType<V> codeType;

    public PropertyCode(@NotNull String name, @NotNull Class<T> entity, @NotNull CodeType<V> codeType) {
        super(name, entity, Integer.class, Types.INTEGER,
                Map.of(Property.ConverterType.java2jdbc, (Code o) -> o == null ? null : o.id(), Property.ConverterType.jdbc2java,
                        (Integer o) -> o == null ? null : codeType.findCode(o).orElse(null), Property.ConverterType.java2text,
                        (Code o) -> o == null ? null : Integer.toString(o.id()), Property.ConverterType.text2java,
                        (String o) -> codeType.findCode(Integer.parseInt(o)).orElse(null)));
        this.codeType = codeType;
    }
}
