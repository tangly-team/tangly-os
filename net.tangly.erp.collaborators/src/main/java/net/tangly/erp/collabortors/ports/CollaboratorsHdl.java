/*
 * Copyright 2022-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.collabortors.ports;

import net.tangly.erp.collabortors.services.CollaboratorsHandler;
import net.tangly.erp.collabortors.services.CollaboratorsRealm;

public class CollaboratorsHdl implements CollaboratorsHandler {
    private final CollaboratorsRealm realm;

    public CollaboratorsHdl(CollaboratorsRealm realm) {
        this.realm = realm;
    }

    @Override
    public void importEntities() {

    }

    @Override
    public void exportEntities() {

    }

    @Override
    public CollaboratorsRealm realm() {
        return realm;
    }
}