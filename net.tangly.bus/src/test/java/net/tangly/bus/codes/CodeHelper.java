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

package net.tangly.bus.codes;

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
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

class CodeHelper {
    /**
     * Private constructor for a utility class.
     */
    private CodeHelper() {
    }

    /**
     * Defines the signature of a code class constructor.
     */
    @FunctionalInterface
    public interface CodeFactory<T extends Code> {
        T create(int id, String code, boolean enabled);
    }

    /**
     * Utility method to read all code values from an enumeration type implementing the {}@link Code} interface.
     *
     * @param clazz   class of the reference code
     * @param factory placeholder to pass the class constructor as lambda expression
     **/
    public static <T extends Enum<T> & Code> CodeType<T> build(Class<T> clazz, CodeFactory<T> factory) {
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
    public static <T extends Code> CodeType<T> build(Class<T> clazz, CodeFactory<T> factory, DataSource dataSource, String tableName) throws
            SQLException {
        final String SQL_QUERY = "SELECT id, code, enabled FROM " + tableName;
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SQL_QUERY)) {
            List<T> codes = new ArrayList<>();
            while (resultSet.next()) {
                T code = factory.create(resultSet.getInt("id"), resultSet.getString("code"), resultSet.getBoolean("enabled"));
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
     * @param path    path to the JSON file containing the code values
     * @param <T>     class of the reference code
     * @return code type and all its values
     * @throws IOException if a file access error occurred
     */
    public static <T extends Code> CodeType<T> build(Class<T> clazz, CodeFactory<T> factory, Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Iterator<Object> iter = new JSONArray(new JSONTokener(reader)).iterator();
            List<T> codes = new ArrayList<>();
            while (iter.hasNext()) {
                JSONObject value = (JSONObject) iter.next();
                T code = factory.create(value.getInt("id"), value.getString("code"), value.getBoolean("enabled"));
                codes.add(code);
            }
            return CodeType.of(clazz, codes);
        }
    }
}
