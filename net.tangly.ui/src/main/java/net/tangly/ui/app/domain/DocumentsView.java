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

import net.tangly.core.domain.Document;
import net.tangly.core.providers.Provider;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class DocumentsView extends ItemView<Document> {
    public DocumentsView(BoundedDomainUi<?> domain, @NotNull Provider<Document> provider) {
        super(Document.class, domain, provider, null, Mode.EDITABLE, false);
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(Document::name).setKey(NAME).setHeader(NAME_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(Document::date).setKey(DATE).setHeader(DATE_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(Document::text).setKey(TEXT).setHeader(TEXT_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
    }
}
