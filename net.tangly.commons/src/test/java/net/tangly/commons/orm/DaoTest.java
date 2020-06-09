/*
 * Copyright 2006-$year Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterEach;

public abstract class DaoTest {
    private static final String dbUrl = "jdbc:hsqldb:mem:tangly;sql.syntax_mys=true";
    private static final String username = "SA";
    private static final String password = "";

    private JDBCDataSource datasource;

    void tearDownDatabase() throws SQLException {
        try (Connection connection = datasource().getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("shutdown");
        }
    }

    protected JDBCDataSource datasource() {
        return datasource;
    }

    protected void setUpDatabase() {
        datasource = new JDBCDataSource();
        datasource.setDatabase(dbUrl);
        datasource.setUser(username);
        datasource.setPassword(password);
        var flyway = Flyway.configure()
                .dataSource(datasource)
                .load();
        flyway.migrate();
    }


}
