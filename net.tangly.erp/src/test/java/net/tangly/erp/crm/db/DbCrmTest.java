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

package net.tangly.erp.crm.db;

import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;

public class DbCrmTest {
    private String SQL_SELECT = "SELECT oid, id, name, fromDate, toDate, text FROM naturalEntities WHERE oid = ?";
    private static String dbUrl = "jdbc:hsqldb:mem:erp";
    private static String username = "SA";
    private static String password = "";

    public DataSource provideDataSource() {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setDatabase(dbUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

}
