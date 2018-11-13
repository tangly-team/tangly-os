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

package net.tangly.commons.activerecords.imp;

import net.tangly.commons.codes.Code;
import net.tangly.commons.codes.CodeType;
import net.tangly.commons.models.HasOid;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PropertyCode<T extends HasOid, V extends Code> extends AbstractProperty<T> {
    private CodeType<V> codeType;

    public PropertyCode(String name, Class<T> entity, CodeType<V> codeType) {
        super(name, entity);
        this.codeType = codeType;
    }

    @Override
    public void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        Code code = (Code) field.get(entity);
        if (code != null) {
            statement.setObject(index, code.id(), Types.INTEGER);
        } else {
            statement.setNull(index, Types.INTEGER);
        }
    }

    @Override
    public void setField(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        Integer codeId = set.getObject(index, Integer.class);
        V code = (codeId == null) ? null : codeType.findCode(codeId).orElse(null);
        field.set(entity, code);
    }
}
