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

package net.tangly.erp.ledger.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import net.tangly.app.Application;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.ports.LedgerEntities;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
class LedgerTest {
    @BeforeEach
    void setup() {
        var realm = new LedgerEntities();
        var domain = new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm), new LedgerAdapter(realm, null, null), Application.instance().registry());
        Application.instance().registerBoundedDomain(domain);
        MockVaadin.setup();
    }

}
