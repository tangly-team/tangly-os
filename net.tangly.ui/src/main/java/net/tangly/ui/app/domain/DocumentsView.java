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

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.core.DateRange;
import net.tangly.core.domain.Document;
import net.tangly.core.providers.Provider;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

/**
 * Documents contain record management informoation and metadata about the document.
 * Only limited attributes can be edited in the view. The description and the tags are editable.
 */
public class DocumentsView extends ItemView<Document> {
    public DocumentsView(BoundedDomainUi<?> domain, @NotNull Provider<Document> provider) {
        super(Document.class, domain, provider, null, Mode.VIEW);
        init();
    }

    public static class DocumentForm extends ItemForm<Document, DocumentsView> {
        private final TextField id;
        private final TextField name;
        private final DatePicker date;
        private final Checkbox generated;
        private final AsciiDocField text;
        private final TagsView tags;

        public DocumentForm(@NotNull BoundedDomainUi<?> domain, @NotNull DocumentsView parent) {
            super(parent);
            id = new TextField("Id");
            name = new TextField("Name");
            date = new DatePicker("Date");
            generated = new Checkbox("Generated");
            text = new AsciiDocField("Text");
            tags = new TagsView(domain, Mode.EDITABLE);
            addTabAt("details", details(), 0);
        }

        public FormLayout details() {
            FormLayout form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(id, name, date, generated, text);
            return form;
        }

        @Override
        public Document createOrUpdateInstance(Document entity) {
            return new Document(id.getValue(), name.getValue(), date.getValue(), new DateRange(null, null), text.getValue(), generated.getValue(), null);
        }
    }

    private void init() {
        var grid = grid();
        grid.addColumn(Document::id).setKey(ID).setHeader(ID_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(Document::name).setKey(NAME).setHeader(NAME_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(Document::date).setKey(DATE).setHeader(DATE_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(Document::text).setKey(TEXT).setHeader(TEXT_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
    }
}
