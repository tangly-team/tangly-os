/*
 * Copyright 2021-2024 Marcel Baumann
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

package net.tangly.ui.pdf;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.server.StreamResource;

import java.io.InputStream;

public class PdfViewerDialog extends Dialog {
    /**
     * Create a PDF viewer for a PDF file s
     *
     * @param name         name of the resource in the Vaadin environment
     * @param resourcePath name of the resource relative to src/main/resources
     */
    public PdfViewerDialog(String name, String resourcePath) {
        this(new StreamResource(name, () -> PdfViewer.class.getResourceAsStream(resourcePath)));
    }

    /**
     * @param name   name of the resource in the Vaadin environment
     * @param stream input stream containing the PDF data to display
     */
    public PdfViewerDialog(String name, InputStream stream) {
        this(new StreamResource(name, () -> stream));
    }

    public PdfViewerDialog(StreamResource resource) {

        PdfViewer viewer = new PdfViewer();
        viewer.setSrc(resource);

        viewer.setHeight("100%");
        setWidth("400px");
        setHeight("150px");
        add(viewer);
        setCloseOnOutsideClick(false);
    }
}
