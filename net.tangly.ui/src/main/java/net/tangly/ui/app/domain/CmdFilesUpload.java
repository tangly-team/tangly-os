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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.AllFinishedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.Handler;
import net.tangly.core.domain.Realm;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;

/**
 * The command to upload a set of files containing entities is delcared. The bounded domain shall import the provided entities.
 * The current supported format is tab seperated values TSV. The entity type is encoded with the filename.
 * The mime type <code>text/tab-separated-values</code>is documented under <a href="https://en.wikipedia.org/wiki/Tab-separated_values">TSV Mime Type</a>.
 * The mime type <code>application/json</code> is documented under <a href="https://en.wikipedia.org/wiki/Media_type">Mime types</a>.
 */
public abstract class CmdFilesUpload<R extends Realm, B, H extends Handler<?>, P> implements Cmd {
    public static final String TSV_MIME = "text/tab-separated-values";
    public static final String JSON_MIME = "application/json";
    public static final String CANCEL = "Cancel";
    public static final String TITLE = "Upload Files";
    protected final BoundedDomain<R, B, H, P> domain;
    private final MultiFileMemoryBuffer buffer;
    private final Upload multiFileUpload;
    private Dialog dialog;

    /**
     * Constructs an upload command for files.
     *
     * @param domain            damain to which entities and aggregates are updated
     * @param acceptedFileTypes the allowed file types to be uploaded
     */
    protected CmdFilesUpload(@NotNull BoundedDomain<R, B, H, P> domain, String... acceptedFileTypes) {
        this.domain = domain;
        buffer = new MultiFileMemoryBuffer();
        multiFileUpload = new Upload(buffer);
        multiFileUpload.setAcceptedFileTypes();
    }

    @Override
    public void execute() {
        dialog = new Dialog();
        dialog.setWidth("2oem");
        var component = new VerticalLayout();
        component.add(multiFileUpload);
        dialog.add(component);
        dialog.setHeaderTitle(TITLE);
        dialog.getFooter().add(new Button(CANCEL, e -> dialog.close()));
        dialog.open();
    }

    protected MultiFileMemoryBuffer buffer() {
        return buffer;
    }

    protected void close() {
        dialog.close();
        multiFileUpload.clearFileList();
        dialog = null;
    }

    /**
     * Registers the listener in charge to load and update the provided entities for the domain. The listene shall define the correct import order and handle missing files.
     * The method could be called in the constructor of the subclass.
     *
     * @param listener listener to register.
     */
    protected void registerAllFinishedListener(@NotNull ComponentEventListener<AllFinishedEvent> listener) {
        multiFileUpload.addAllFinishedListener(listener);
    }

    /**
     * Processes the input stream with the provided consumer.
     *
     * @param filename name of the file uploaded in the buffer, which is processed
     * @param consumer consumer processing the input file
     */
    protected void processInputStream(@NotNull String filename, @NotNull BiConsumer<Reader, String> consumer) {
        try (Reader reader = new BufferedReader(new InputStreamReader(buffer.getInputStream(filename)))) {
            consumer.accept(reader, filename);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
