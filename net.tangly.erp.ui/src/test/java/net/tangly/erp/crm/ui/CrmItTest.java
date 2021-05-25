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

package net.tangly.erp.crm.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import net.tangly.core.TypeRegistry;
import net.tangly.erpr.crm.ports.CrmEntities;
import net.tangly.erpr.crm.ports.CrmHdl;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.crm.services.CrmRealm;
import org.junit.jupiter.api.BeforeEach;

class CrmItTest {
    static CrmBoundedDomain ofDomain() {
        CrmRealm realm = new CrmEntities();
        return new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmHdl(realm, null), null, new TypeRegistry());
    }

    @BeforeEach
    void setup() {
        MockVaadin.setup();
    }

}
