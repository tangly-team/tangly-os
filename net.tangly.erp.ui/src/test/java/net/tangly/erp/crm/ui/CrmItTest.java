/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import net.tangly.erp.Erp;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Defines basic infrastructure for integration tests of the bounded domain with karibu testing framewwork and in-memory database without persistence.
 */
class CrmItTest {
    private static Routes routes;

    @BeforeAll
    public static void createModelAndRoutes() {
        Erp.inMemoryErp();
        routes = new Routes().autoDiscoverViews("net.tangly.erp.ui");
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }
}
