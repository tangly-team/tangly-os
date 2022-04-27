/*
 * Copyright 2022-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erpr.crm.ports.CrmHdl;
import net.tangly.erpr.crm.ports.CrmTsvHdl;
import net.tangly.ui.app.domain.CmdDialog;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

/**
 * Command to upload a set of files containing entities. The domain will import the provided entities.
 * The current supported format is TSV. The entity type is encoded with the filename.
 */
public class CmdFilesUpload extends CmdDialog {
    public static final String CANCEL = "Cancel";

    private final CrmBoundedDomain domain;
    private final MultiFileMemoryBuffer multiFileMemoryBuffer;
    private final Upload multiFileUpload;

    CmdFilesUpload(@NotNull CrmBoundedDomain domain) {
        super("2oem");
        this.domain = domain;
        multiFileMemoryBuffer = new MultiFileMemoryBuffer();
        multiFileUpload = new Upload(multiFileMemoryBuffer);
        multiFileUpload.setAcceptedFileTypes("text/tsv");
    }

    protected Component form() {
        var component = new Div();
        component.add(multiFileUpload);
        Button cancel = new Button(CANCEL, e -> close());
        multiFileUpload.addAllFinishedListener(event -> {
            var handler = new CrmTsvHdl(domain.realm());
            Set<String> files = multiFileMemoryBuffer.getFiles();
            if (files.contains(CrmHdl.LEADS_TSV)) {
                handler.importLeads(createReader(CrmHdl.LEADS_TSV), CrmHdl.LEADS_TSV);
            } else if (files.contains(CrmHdl.NATURAL_ENTITIES_TSV)) {
                handler.importNaturalEntities(createReader(CrmHdl.NATURAL_ENTITIES_TSV), CrmHdl.NATURAL_ENTITIES_TSV);
            } else if (files.contains(CrmHdl.LEGAL_ENTITIES_TSV)) {
                handler.importNaturalEntities(createReader(CrmHdl.LEGAL_ENTITIES_TSV), CrmHdl.LEGAL_ENTITIES_TSV);
            } else if (files.contains(CrmHdl.EMPLOYEES_TSV)) {
                handler.importEmployees(createReader(CrmHdl.EMPLOYEES_TSV), CrmHdl.EMPLOYEES_TSV);
            } else if (files.contains(CrmHdl.CONTRACTS_TSV)) {
                handler.importContracts(createReader(CrmHdl.CONTRACTS_TSV), CrmHdl.CONTRACTS_TSV);
            } else if (files.contains(CrmHdl.INTERACTIONS_TSV)) {
                handler.importInteractions(createReader(CrmHdl.INTERACTIONS_TSV), CrmHdl.INTERACTIONS_TSV);
            } else if (files.contains(CrmHdl.ACTIVITIES_TSV)) {
                handler.importActivities(createReader(CrmHdl.ACTIVITIES_TSV), CrmHdl.ACTIVITIES_TSV);
            } else if (files.contains(CrmHdl.SUBJECTS_TSV)) {
                handler.importSubjects(createReader(CrmHdl.SUBJECTS_TSV), CrmHdl.SUBJECTS_TSV);
            } else if (files.contains(CrmHdl.COMMENTS_TSV)) {
                handler.importComments(createReader(CrmHdl.COMMENTS_TSV), CrmHdl.COMMENTS_TSV);
            }
        });
        return component;
    }

    private Reader createReader(@NotNull String inputName) {
        return new BufferedReader(new InputStreamReader(multiFileMemoryBuffer.getInputStream(inputName)));
    }
}
