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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.core.DateRange;
import net.tangly.core.Tag;
import net.tangly.core.domain.Document;
import net.tangly.core.domain.Operation;
import net.tangly.core.providers.Provider;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Documents contain record management informoation and metadata about the document.
 * Only limited attributes can be edited in the view. The description and the tags are editable.
 */
public class DocumentsView extends ItemView<Document> {

    public static class DocumentForm extends ItemForm<Document, DocumentsView> {
        private final TextField id;
        private final TextField name;
        private final TextField extension;
        private final DateTimePicker time;
        private final Checkbox generated;
        private final AsciiDocField text;
        private final One2ManyOwnedField<Tag> tags;
        private final BoundedDomainUi<?> domain;

        public DocumentForm(@NotNull BoundedDomainUi<?> domain, @NotNull DocumentsView parent) {
            super(parent);
            this.domain = domain;
            id = new TextField("Id");
            id.setReadOnly(true);
            name = new TextField("Name");
            extension = new TextField("Extension");
            extension.setReadOnly(true);
            time = new DateTimePicker("Date");
            time.setReadOnly(true);
            generated = new Checkbox("Generated");
            text = new AsciiDocField("Text");
            tags = new One2ManyOwnedField<>(new TagsView(parent.domainUi(), parent.mode()));
            addTabAt("details", details(), 0);
            addTabAt("tags", tags, 1);

        }

        public FormLayout details() {
            FormLayout form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(name, id, extension, time, generated, new HtmlComponent("br"), text);
            form.setColspan(text, 3);
            binder().bindReadOnly(id, Document::id);
            binder().bindReadOnly(name, Document::name);
            binder().bindReadOnly(extension, Document::extension);
            binder().bindReadOnly(time, Document::time);
            binder().bindReadOnly(generated, Document::generated);
            binder().bindReadOnly(text, Document::text);
            binder().bindReadOnly(tags, Document::tags);
            return form;
        }

        @Override
        public Document createOrUpdateInstance(Document entity) {
            return new Document(id.getValue(), name.getValue(), extension.getValue(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                new DateRange(null, null), text.getValue(), generated.getValue(), null);
        }

        @Override
        protected Document deleteEntity() {
            Document document = value();
            Path file = Path.of(domain.domain().directory().docs(domain.domain().name()), document.id() + document.extension());
            try {
                Files.delete(file);
                super.deleteEntity();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return document;
        }
    }

    public DocumentsView(BoundedDomainUi<?> domain, @NotNull Provider<Document> provider, Mode mode) {
        super(Document.class, domain, provider, null, mode);
        form(() -> new DocumentForm(domain, this));
        init();
    }

    @Override
    protected void buildMenu() {
        if (mode() != Mode.LIST) {
            menu().add(Mode.VIEW_TEXT, e ->
                e.getItem().ifPresent(o -> form().get().display(o)), GridMenu.MenuItemType.ITEM);
        }
        if (!mode().readonly()) {
            menu().add(new Hr());
            menu().add(Operation.EDIT_TEXT, event -> event.getItem().ifPresent(o -> form().get().edit(o)), GridMenu.MenuItemType.ITEM);
            menu().add(Operation.DELETE_TEXT, event -> event.getItem().ifPresent(o -> form().get().delete(o)), GridMenu.MenuItemType.ITEM);
            menu().add(Operation.REFRESH_TEXT, _ -> refresh(), GridMenu.MenuItemType.GLOBAL);
        }
        addActions(menu());
    }

    private void init() {
        var grid = grid();
        grid.addColumn(Document::name).setKey(NAME).setHeader(NAME_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(Document::time).setKey(TIME).setHeader(TIME_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(VaadinUtils.addLinkToFile(Path.of(domain().directory().docs(domain().name())))).setKey(ID).setHeader(ID_LABEL).setSortable(true)
            .setAutoWidth(true).setResizable(true);
        grid.addColumn(Document::text).setKey(TEXT).setHeader(TEXT_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
    }
}
