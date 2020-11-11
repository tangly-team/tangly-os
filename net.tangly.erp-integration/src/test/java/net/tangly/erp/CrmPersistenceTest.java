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

package net.tangly.erp;

import java.nio.file.Path;

import net.tangly.bus.crm.CrmRealm;
import net.tangly.crm.ports.CrmEntities;
import net.tangly.crm.ports.CrmHdl;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrmPersistenceTest {
    public static final String PATH = "/Users/Shared/tmp/db";
    @Test
    void persistCrmRealTest() {
        CrmRealm realm = new CrmEntities();
        CrmHdl crmHdl = new CrmHdl(realm, Path.of("/Users/Shared/tangly/", "crm"));
        crmHdl.importEntities();
        assertThat(realm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(realm.legalEntities().items().isEmpty()).isFalse();
        assertThat(realm.employees().items().isEmpty()).isFalse();
        assertThat(realm.contracts().items().isEmpty()).isFalse();
        assertThat(realm.interactions().items().isEmpty()).isFalse();

        EmbeddedStorageManager storageManager = EmbeddedStorage.start(Path.of(PATH));
        storageManager.setRoot(realm);
        storageManager.storeRoot();
        storageManager.shutdown();

        storageManager = EmbeddedStorage.start(Path.of(PATH));
        Object persistentRealm = storageManager.root();
        System.out.println(persistentRealm);
        assertThat(persistentRealm instanceof CrmRealm).isTrue();
        realm = (CrmRealm) persistentRealm;
        assertThat(realm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(realm.legalEntities().items().isEmpty()).isFalse();
        assertThat(realm.employees().items().isEmpty()).isFalse();
        assertThat(realm.contracts().items().isEmpty()).isFalse();
        assertThat(realm.interactions().items().isEmpty()).isFalse();

        storageManager.shutdown();
    }
}
