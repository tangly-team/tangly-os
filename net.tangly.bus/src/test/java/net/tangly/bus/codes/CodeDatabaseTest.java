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

package net.tangly.bus.codes;

import org.flywaydb.core.Flyway;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class CodeDatabaseTest {
    private static final String dbUrl = "jdbc:hsqldb:mem:tangly;sql.syntax_mys=true";
    private static final String username = "SA";
    private static final String password = "";
    private JDBCDataSource datasource;


    @BeforeEach
    void setUp() throws NoSuchMethodException {
        datasource = new JDBCDataSource();
        datasource.setDatabase(dbUrl);
        datasource.setUser(username);
        datasource.setPassword(password);
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
        CodeType<TestCode> type = CodeHelper.build(TestCode.class, TestCode::new, datasource, "tangly.dbCode");
        assertThat(type.activeCodes().size()).isEqualTo(5);
        assertThat(type.findCode(0).isPresent()).isTrue();
        assertThat(type.findCode(1).isPresent()).isTrue();
        assertThat(type.findCode(2).isPresent()).isTrue();
        assertThat(type.findCode(3).isPresent()).isTrue();
        assertThat(type.findCode(4).isPresent()).isTrue();
        assertThat(type.findCode(5).isPresent()).isFalse();
    }
}
