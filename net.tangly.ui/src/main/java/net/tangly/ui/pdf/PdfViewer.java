/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.pdf;

import java.io.InputStream;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.alejandro.PdfBrowserViewer;

public class PdfViewer extends Dialog {
    /**
     * Create a PDF viewer for a PDF file s
     * @param name         name of the resource in the Vaadin environment
     * @param resourcePath name of the resource relative to src/main/resources
     */
    public PdfViewer(String name, String resourcePath) {
        this(new StreamResource(name, () -> PdfViewer.class.getResourceAsStream(resourcePath)));
    }

    /**
     * @param name   name of the resource in the Vaadin environment
     * @param stream input stream containing the pdf data to display
     */
    public PdfViewer(String name, InputStream stream) {
        this(new StreamResource(name, () -> stream));
    }

    public PdfViewer(StreamResource resource) {
        PdfBrowserViewer viewer = new PdfBrowserViewer(resource);
        viewer.setHeight("100%");
        setWidth("400px");
        setHeight("150px");
        add(viewer);
        setCloseOnOutsideClick(false);
    }
}