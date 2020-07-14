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

package net.tangly.commons.vaadin;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UiTests {
    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(new Routes().autoDiscoverViews("net.tangly.commons.ui"));
    }

    @Test
    void test() {
        // final MainView main = (MainView) UI.getCurrent().getChildren().findFirst().get();
        // assertThat(main.getChildren().count()).isGreaterThan(0);
        // TODO wait until vaadin gradle plugin does support JDK 14 and Vaadin 16.x.x
    }
}
