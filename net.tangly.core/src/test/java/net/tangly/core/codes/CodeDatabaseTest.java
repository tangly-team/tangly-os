/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.codes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeDatabaseTest {
    private static final String DB_URL = "jdbc:hsqldb:mem:tangly";
    private static final String USERNAME = "SA";
    private static final String PASSWORD = "";
    private JDBCDataSource datasource;


    @BeforeEach
    void setUp() {
        datasource = new JDBCDataSource();
        datasource.setDatabase(DB_URL);
        datasource.setUser(USERNAME);
        datasource.setPassword(PASSWORD);
        var flyway = Flyway.configure().dataSource(datasource).load();
        flyway.migrate();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection connection = datasource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("shutdown");
        }
    }

    @Test
    void testTestCodes() throws SQLException {
        var type = CodeHelper.build(TestCode.class, TestCode::new, datasource, "tangly.dbCode");
        assertThat(type.activeCodes().size()).isEqualTo(5);
        assertThat(type.findCode(0)).isPresent();
        assertThat(type.findCode(1)).isPresent();
        assertThat(type.findCode(2)).isPresent();
        assertThat(type.findCode(3)).isPresent();
        assertThat(type.findCode(4)).isPresent();
        assertThat(type.findCode(5)).isNotPresent();

        // test basic operations
        TestCode code1 = type.findCode(1).orElse(null);
        TestCode code2 = type.findCode(2).orElse(null);
        assertThat(code1).isNotNull();
        assertThat(code2).isNotNull();
        assertThat(code1).isNotEqualTo(code2);
        assertThat(code1.hashCode()).isNotEqualTo(code2.hashCode());
        assertThat(code1.toString()).contains("1");
    }
}

