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

package net.tangly.erp.collabortors.ui;

import net.tangly.erp.collabortors.ports.CollaboratorsAdapter;
import net.tangly.erp.collabortors.ports.CollaboratorsTsvHdl;
import net.tangly.erp.collabortors.services.CollaboratorsBoundedDomain;
import net.tangly.erp.collabortors.services.CollaboratorsBusinessLogic;
import net.tangly.erp.collabortors.services.CollaboratorsPort;
import net.tangly.erp.collabortors.services.CollaboratorsRealm;
import net.tangly.ui.app.domain.CmdFilesUpload;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CmdFilesUploadCollaborators extends CmdFilesUpload<CollaboratorsRealm, CollaboratorsBusinessLogic, CollaboratorsPort> {
    CmdFilesUploadCollaborators(@NotNull CollaboratorsBoundedDomain domain) {
        super(domain, CmdFilesUpload.TSV_MIME);
        registerAllFinishedListener(
            (event -> {
                var handler = new CollaboratorsTsvHdl(domain.realm());
                Set<String> files = buffer().getFiles();
                if (files.contains(CollaboratorsAdapter.COLLABORATORS_TSV)) {
                    processInputStream(CollaboratorsAdapter.COLLABORATORS_TSV, handler::importCollaboratators);
                }
                close();
            }));
    }
}
