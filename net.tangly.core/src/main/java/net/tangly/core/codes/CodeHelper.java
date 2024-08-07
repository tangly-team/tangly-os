/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core.codes;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public final class CodeHelper {
    /**
     * Private constructor for a utility class.
     */
    private CodeHelper() {
    }

    /**
     * Utility method to read all code values from an enumeration type implementing the {}@link Code} interface.
     *
     * @param clazz class of the reference code
     **/
    public static <T extends Enum<T> & Code> CodeType<T> build(Class<T> clazz) {
        return CodeType.of(clazz);
    }

    /**
     * Utility method to read all code values from a relational database table using Java regular API.
     *
     * @param clazz      class of the reference code
     * @param factory    placeholder to pass the class constructor as lambda expression
     * @param dataSource data source to the database to read from
     * @param tableName  name of the table containing the code values
     * @param <T>        class of the reference code
     * @return code type and all its values
     * @throws SQLException if a database access error occurred
     */
    public static <T extends Code> CodeType<T> build(Class<T> clazz, Function<ResultSet, T> factory, DataSource dataSource, String tableName)
        throws SQLException {
        final String SQL_QUERY = "SELECT id, code, enabled FROM %s".formatted(tableName);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SQL_QUERY)) {
            List<T> codes = new ArrayList<>();
            while (resultSet.next()) {
                T code = factory.apply(resultSet);
                codes.add(code);
            }
            return CodeType.of(clazz, codes);
        }
    }

    /**
     * Utility method to read all code values from a JSON file using the org.json library.
     *
     * @param clazz   class of the reference code
     * @param factory placeholder to pass the class constructor as lambda expression
     * @param path    uri to the JSON file containing the code values
     * @param <T>     class of the reference code
     * @return code type and all its values
     * @throws IOException if a file access error occurred
     */
    public static <T extends Code> CodeType<T> build(Class<T> clazz, Function<JSONObject, T> factory, Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Iterator<Object> iter = new JSONArray(new JSONTokener(reader)).iterator();
            List<T> codes = new ArrayList<>();
            while (iter.hasNext()) {
                JSONObject value = (JSONObject) iter.next();
                T code = factory.apply(value);
                codes.add(code);
            }
            return CodeType.of(clazz, codes);
        }
    }
}
