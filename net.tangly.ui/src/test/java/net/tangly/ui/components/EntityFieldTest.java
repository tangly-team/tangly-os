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

package net.tangly.ui.components;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import net.tangly.core.MutableEntity;
import net.tangly.core.MutableEntityExtended;
import net.tangly.core.MutableEntityExtendedImp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test needs karibu mocking because DateTimePicker retrieve locale instance through UI instance.
 */
class EntityFieldTest {
    static class MyEntity extends MutableEntityExtendedImp {

        protected MyEntity(long oid) {
            super(oid);
        }
    }

    private static Routes routes;

    @BeforeAll
    public static void createRoutes() {
        routes = new Routes().autoDiscoverViews("net.tangly.ui");
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }

    @Test
    void testEntityField() {
        final EntityField<MutableEntityExtended> entityField = new EntityField<>();
        entityField.setValue(null);
        assertThat(entityField.getValue()).isNull();

        var entity = new MyEntity(MutableEntity.UNDEFINED_OID);
        entityField.setValue(entity);
        assertThat(entityField.getValue()).isNotNull();
    }
}
