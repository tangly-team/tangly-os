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

package net.tangly.apps.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.Route;
import net.tangly.app.Application;
import net.tangly.app.ApplicationView;
import net.tangly.app.Tenant;
import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.core.domain.User;
import net.tangly.ui.app.domain.Cmd;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Properties;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Defines basic infrastructure for integration tests of the bounded domain with karibu testing framewwork and in-memory database without persistence.
 */
@Route("")
class AppsTest extends ApplicationView {
    public static final String TENANT = "test";
    private static Routes routes;

    public AppsTest() {
        Tenant tenant = createAndRegisterTenant(TENANT);
        super(tenant, false);
        drawerMenu(createUser());
        selectBoundedDomainUi(AppsBoundedDomain.DOMAIN);
    }

    @BeforeAll
    public static void createModelAndRoutes() {
        routes = new Routes().autoDiscoverViews("net.tangly.apps.ui");
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }

    protected AppsBoundedDomain apps() {
        return Application.instance().tenant(TENANT).apps();
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

    private static Tenant createAndRegisterTenant(@NotNull String tenantId) {
        Properties properties = new Properties();
        properties.setProperty(Tenant.TENANT_ID_PROPERTY, tenantId);
        Tenant tenant = new Tenant(properties);
        Application.instance().putTenant(tenant);
        return tenant;
    }

    private static User createUser() {
        final String username = "aeon";
        String passwordSalt = User.newSalt();
        String passwordHash = User.encryptPassword("aeon", passwordSalt);
        var rights = List.of(new AccessRights(username, AppsBoundedDomain.DOMAIN, AccessRightsCode.domainAdmin));
        return new User(username, passwordHash, passwordSalt, true, null, rights, null);
    }
}
