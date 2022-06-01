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

package net.tangly.erp.invoices.ports;

import net.tangly.commons.logger.EventData;
import net.tangly.erp.invoices.artifacts.InvoiceJson;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.ArticleCode;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesRealm;
import net.tangly.erp.ports.TsvHdl;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.tangly.erp.ports.TsvHdl.ID;
import static net.tangly.erp.ports.TsvHdl.MODULE;
import static net.tangly.erp.ports.TsvHdl.NAME;
import static net.tangly.erp.ports.TsvHdl.TEXT;
import static net.tangly.erp.ports.TsvHdl.get;

/**
 * Provide import and export functions for invoice entities persisted in tab separated files.
 */
public class InvoicesTsvJsonHdl {
    private final InvoicesRealm realm;

    @Inject
    public InvoicesTsvJsonHdl(@NotNull InvoicesRealm realm) {
        this.realm = realm;
    }

    private static TsvEntity<Article> createTsvArticle() {
        Function<CSVRecord, Article> imports = (CSVRecord obj) -> new Article(get(obj, ID), get(obj, NAME), get(obj, TEXT),
            Enum.valueOf(ArticleCode.class, get(obj, "code").toLowerCase()), TsvProperty.CONVERT_BIG_DECIMAL_FROM.apply(get(obj, "unitPrice")),
            get(obj, "unit"), TsvProperty.CONVERT_BIG_DECIMAL_FROM.apply(get(obj, "vatRate")));

        List<TsvProperty<Article, ?>> fields = List.of(TsvProperty.ofString(ID, Article::id, null), TsvProperty.ofString(NAME, Article::name, null),
            TsvProperty.ofString(TEXT, Article::text, null), TsvProperty.ofEnum(ArticleCode.class, "code", Article::code, null),
            TsvProperty.ofBigDecimal("unitPrice", Article::unitPrice, null), TsvProperty.ofString("unit", Article::unit, null),
            TsvProperty.ofBigDecimal("vatRate", Article::vatRate, null));
        return TsvEntity.of(Article.class, fields, imports);
    }

    public void exportArticles(@NotNull Path path) {
        TsvHdl.exportEntities(path, createTsvArticle(), realm.articles());
    }

    public void importArticles(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(reader, source, createTsvArticle(), realm.articles());
    }

    public void importArticles(@NotNull Path path) {
        TsvHdl.importEntities(path, createTsvArticle(), realm.articles());
    }

    public Invoice importInvoice(@NotNull Reader reader, String source) {
        var invoiceJson = new InvoiceJson(realm);
        var invoice = invoiceJson.imports(reader, source);
        if ((invoice != null) && invoice.check()) {
            realm.invoices().update(invoice);
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.SUCCESS, "Imported Invoice", Map.ofEntries(Map.entry("invoice", invoice)));
            return invoice;
        } else {
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.WARNING, "Invalid Invoice", Map.ofEntries(Map.entry("invoice", source)));
            return null;
        }
    }

}
