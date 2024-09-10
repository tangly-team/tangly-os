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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.AllFinishedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import net.tangly.commons.lang.functional.TriConsumer;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.Port;
import net.tangly.core.domain.Realm;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * The command to upload a set of files containing entities is declared. The bounded domain shall import the provided entities.
 * The current supported format is tab seperated values TSV. The entity type is encoded with the filename.
 * The mime type <code>text/tab-separated-values</code>is documented under <a href="https://en.wikipedia.org/wiki/Tab-separated_values">TSV Mime Type</a>.
 * The mime type <code>application/json</code> is documented under <a href="https://en.wikipedia.org/wiki/Media_type">Mime types</a>.
 */
public abstract class CmdFilesUpload<R extends Realm, B, P extends Port<R>> implements Cmd {
    public static final String TSV_MIME = "text/tab-separated-values";
    public static final String JSON_MIME = "application/json";
    public static final String CANCEL = "Cancel";
    public static final String TITLE = "Upload Files";
    protected final BoundedDomain<R, B, P> domain;
    private final MultiFileMemoryBuffer buffer;
    private final Upload upload;
    private final Span errorField;
    private final Checkbox overwrite;
    private Dialog dialog;

    /**
     * Constructs an upload command for files.
     *
     * @param domain            domain to which entities and aggregates are updated
     * @param acceptedFileTypes the allowed file types to be uploaded
     */
    protected CmdFilesUpload(@NotNull BoundedDomain<R, B, P> domain, String... acceptedFileTypes) {
        this.domain = domain;
        buffer = new MultiFileMemoryBuffer();
        upload = new Upload(buffer);
        errorField = new Span();
        overwrite = new Checkbox("Overwrite");
        upload.setAcceptedFileTypes();
    }

    @Override
    public void execute() {
        dialog = new Dialog();
        dialog.setWidth("2oem");
        var component = new VerticalLayout();
        errorField.setVisible(false);
        errorField.getStyle().set("color", "red");

        upload.addFailedListener(e -> showErrorMessage(e.getReason().getMessage()));
        upload.addFileRejectedListener(e -> showErrorMessage(e.getErrorMessage()));

        component.add(upload, errorField);
        dialog.add(component);
        dialog.setHeaderTitle(TITLE);
        Button close = new Button("Close", VaadinIcon.COGS.create(), e -> dialog.close());
        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();
            Notification notification = Notification.show(errorMessage, 5000,
                Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        dialog.getFooter().add(new Button(CANCEL, e -> dialog.close()), close);
        dialog.open();
    }

    public boolean overwrite() {
        return overwrite.getValue();
    }

    protected BoundedDomain<R, B, P> domain() {
        return domain;
    }
    protected MultiFileMemoryBuffer buffer() {
        return buffer;
    }

    protected void close() {
        dialog.close();
        upload.clearFileList();
        dialog = null;
    }

    /**
     * Register the listener in charge to load and update the provided entities for the domain. The listener shall define the correct import order and handle
     * missing files.
     * The method could be called in the constructor of the subclass.
     *
     * @param listener listener to register.
     */
    protected void registerAllFinishedListener(@NotNull ComponentEventListener<AllFinishedEvent> listener) {
        upload.addAllFinishedListener(listener);
    }

    /**
     * Process the input stream with the provided consumer.
     *
     * @param filename name of the file uploaded in the buffer, which is processed
     * @param consumer consumer processing the input file
     */
    protected void processInputStream(@NotNull DomainAudit audit, @NotNull String filename, @NotNull TriConsumer<DomainAudit, Reader, String> consumer) {
        try (Reader reader = new BufferedReader(new InputStreamReader(buffer.getInputStream(filename)))) {
            consumer.accept(audit, reader, filename);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Process the input stream with the provided consumer.
     *
     * @param filename name of the file uploaded in the buffer, which is processed
     * @param consumer consumer processing the input file
     */
    protected void processInputStream(@NotNull DomainAudit audit, @NotNull String filename, boolean overwrite,
                                      @NotNull TriConsumer<DomainAudit, Reader, String> consumer) {
        try (Reader reader = new BufferedReader(new InputStreamReader(buffer.getInputStream(filename)))) {
            consumer.accept(audit, reader, filename);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void showErrorMessage(String message) {
        errorField.setVisible(true);
        errorField.setText(message);
    }
}
