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

package net.tangly.erp;

import java.nio.file.Path;

import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrmPersistenceTest {
    @Test
    @Tag("localTest")
    void persistCrmRealLocalTest() {
        String PATH = "/Users/Shared/tangly/db/crm";
        var realm = new CrmEntities(Path.of(PATH));
        realm.storeRoot();
        CrmHdl crmHdl = new CrmHdl(realm, Path.of("/Users/Shared/tangly/", "import/crm"));
        crmHdl.importEntities();
        assertThat(realm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(realm.legalEntities().items().isEmpty()).isFalse();
        assertThat(realm.employees().items().isEmpty()).isFalse();
        assertThat(realm.contracts().items().isEmpty()).isFalse();
        assertThat(realm.interactions().items().isEmpty()).isFalse();
        realm.storeRoot();
        realm.shutdown();

        var persistentRealm = new CrmEntities(Path.of(PATH));
        assertThat(persistentRealm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(persistentRealm.legalEntities().items().isEmpty()).isFalse();
        assertThat(persistentRealm.employees().items().isEmpty()).isFalse();
        assertThat(persistentRealm.contracts().items().isEmpty()).isFalse();
        assertThat(persistentRealm.interactions().items().isEmpty()).isFalse();
        persistentRealm.shutdown();
    }
}
