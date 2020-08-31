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

package net.tangly.crm.ports;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;


/**
 * Test the business logic of the ledger domain model. An in-memory file system is set with a Swiss ledger definition and transactions files.
 */
class LedgerBusinessLogicTest {
    void createLedgerReportTest() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            CrmAndLedgerStore crmAndLedgerStore = new CrmAndLedgerStore(fs);
            crmAndLedgerStore.createCrmAndLedgerRepository();
            Path directory = crmAndLedgerStore.crmRoot();
        }
    }

    public void turnoverEbitAndEarningsTest() {

    }
}
