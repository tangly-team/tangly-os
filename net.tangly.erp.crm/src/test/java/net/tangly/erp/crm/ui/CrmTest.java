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

package net.tangly.erp.crm.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import net.tangly.app.Application;
import net.tangly.erp.crm.ports.CrmAdapter;
import net.tangly.erp.crm.ports.CrmEntities;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.ui.app.domain.Cmd;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Defines basic infrastructure for integration tests of the bounded domain with karibu testing framewwork and in-memory database without persistence.
 */
class CrmTest {
    private static Routes routes;

    @BeforeAll
    public static void createModelAndRoutes() {
        routes = new Routes().autoDiscoverViews("net.tangly.erp.crm.ui");
    }

    @BeforeEach
    public void setupVaadin() {
        var realm = new CrmEntities();
        var domain = new CrmBoundedDomain(realm, new CrmBusinessLogic(realm), new CrmAdapter(realm, Path.of(Application.instance().imports(CrmBoundedDomain.DOMAIN))),
            Application.instance().registry());
        Application.instance().registerBoundedDomain(domain);
        MockVaadin.setup(routes);
    }

    /**
     * Return the form layout of the dialog associated with the command. This method is provided because currently find all dialogs karibu functionality is not working as exepcted.
     *
     * @param cmd command which form should be returned
     * @return requested form layout of the command dialog
     */
    protected Component form(Cmd cmd) {
        Dialog dialog = cmd.dialog();
        assertThat(dialog).isNotNull();
        Component form = _get(dialog, FormLayout.class);
        assertThat(form).isNotNull();
        return form;
    }

    protected CrmBoundedDomain crmBoundedDomain() {
        return (CrmBoundedDomain) Application.instance().getBoundedDomain(CrmBoundedDomain.DOMAIN).orElseThrow();
    }

}