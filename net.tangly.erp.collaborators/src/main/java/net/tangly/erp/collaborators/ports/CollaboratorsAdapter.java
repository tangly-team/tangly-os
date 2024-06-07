/*
 * Copyright 2022-2024 Marcel Baumann
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

package net.tangly.erp.collaborators.ports;

import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.Port;
import net.tangly.erp.collabortors.services.CollaboratorsPort;
import net.tangly.erp.collabortors.services.CollaboratorsRealm;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class CollaboratorsAdapter implements CollaboratorsPort {
    public static final String COLLABORATORS_TSV = "collaborators.tsv";
    private final CollaboratorsRealm realm;
    private final Path dataFolder;

    public CollaboratorsAdapter(CollaboratorsRealm realm, @NotNull Path dataFolder) {
        this.realm = realm;
        this.dataFolder = dataFolder;
    }

    @Override
    public CollaboratorsRealm realm() {
        return realm;
    }

    @Override
    public void importEntities(@NotNull DomainAudit audit) {
        var handler = new CollaboratorsTsvHdl(realm());
        handler.importCollaboratators(audit, dataFolder.resolve(COLLABORATORS_TSV));
    }

    @Override
    public void exportEntities(@NotNull DomainAudit audit) {
        var handler = new CollaboratorsTsvHdl(realm());
        handler.exportCollaborators(audit, dataFolder.resolve(COLLABORATORS_TSV));
    }

    @Override
    public void clearEntities(@NotNull DomainAudit audit) {
        realm().collaborators().deleteAll();
        Port.entitiesCleared(audit, "collaborators");
    }
}

