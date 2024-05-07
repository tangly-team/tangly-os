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

package net.tangly.erp.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class MainLayoutTest {
    private static Routes routes;

    @BeforeAll
    static void createRoutes() {
        MainView.create();
        routes = new Routes().autoDiscoverViews("net.tangly.erp.ui");
    }

    @BeforeEach
    void setupVaadin() {
        MockVaadin.setup(routes);
    }

    @AfterEach
    void tearDownVaadin() {
        MockVaadin.tearDown();
    }

    @Test
    @Disabled
    void mainViewTest() {
        final MainView main = (MainView) UI.getCurrent().getChildren().findFirst().get();
        assertThat(main.getChildren().count()).isEqualTo(6);
    }
}
