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

package net.tangly.apps.ui;

import com.vaadin.flow.server.VaadinSession;
import net.tangly.app.ui.CmdLogout;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("IntegrationTest")
@Disabled
class CmdLogoutTest extends AppsTest {
    @Test
    void testLogout() {
        var logout = new CmdLogout(this);
        logout.execute();
        assertThat(VaadinSession.getCurrent().getSession()).isNotNull();
    }
}
