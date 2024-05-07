/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.erp.crm.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.app.Application;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmEntities;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("IntegrationTest")
class CmdLogoutTest {
    private static Routes routes;

    @BeforeAll
    public static void createModelAndRoutes() {
        routes = new Routes().autoDiscoverViews("net.tangly.erp.ui");
    }

    @BeforeEach
    public void setupVaadin() {
        var realm = new CrmEntities();
        var domain = new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmAdapter(realm, Path.of(Application.instance().imports(CrmBoundedDomain.DOMAIN))),
            Application.instance().registry());
        Application.instance().registerBoundedDomain(domain);
        MockVaadin.setup(routes);
    }

    @Test
    void testLogout() {
        var logout = new CmdLogout();
        logout.execute();
        assertThat(VaadinSession.getCurrent().getSession()).isNotNull();
    }
}
