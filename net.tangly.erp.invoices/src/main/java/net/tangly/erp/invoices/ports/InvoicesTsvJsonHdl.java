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

package net.tangly.erp.invoices.ports;

import net.tangly.commons.logger.EventData;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.TsvHdl;
import net.tangly.erp.invoices.artifacts.InvoiceJson;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.ArticleCode;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesRealm;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static net.tangly.core.tsv.TsvHdlCore.NAME;
import static net.tangly.core.tsv.TsvHdlCore.TEXT;
import static net.tangly.gleam.model.TsvEntity.get;

/**
 * Provide import and export functions for invoice entities persisted in tab separated files.
 */
public class InvoicesTsvJsonHdl {
    private final InvoicesRealm realm;

    public InvoicesTsvJsonHdl(@NotNull InvoicesRealm realm) {
        this.realm = realm;
    }

    private static TsvEntity<Article> createTsvArticle() {
        Function<CSVRecord, Article> imports =
            (CSVRecord obj) -> new Article(get(obj, TsvHdl.ID), get(obj, NAME), get(obj, TEXT), Enum.valueOf(ArticleCode.class, get(obj, "code").toLowerCase()),
                TsvProperty.CONVERT_BIG_DECIMAL_FROM.apply(get(obj, "unitPrice")), get(obj, "unit"));

        List<TsvProperty<Article, ?>> fields =
            List.of(TsvProperty.ofString(TsvHdl.ID, Article::id), TsvProperty.ofString(NAME, Article::name), TsvProperty.ofString(TEXT, Article::text),
                TsvProperty.ofEnum(ArticleCode.class, "code", Article::code, null), TsvProperty.ofBigDecimal("unitPrice", Article::unitPrice, null),
                TsvProperty.ofString("unit", Article::unit));
        return TsvEntity.of(Article.class, fields, imports);
    }

    public void exportArticles(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvArticle(), realm.articles());
    }

    public void importArticles(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvArticle(), realm.articles());
    }

    public Invoice importInvoice(@NotNull DomainAudit audit, @NotNull Reader reader, String source) {
        var invoiceJson = new InvoiceJson(realm);
        var invoice = invoiceJson.imports(audit, reader, source);
        if ((invoice != null) && invoice.check()) {
            // locale is not a mandatory field and default locale is English
            if (Objects.isNull(invoice.locale())) {
                invoice.locale(Locale.ENGLISH);
            }
            realm.invoices().update(invoice);
            audit.log(EventData.IMPORT, EventData.Status.SUCCESS, "Imported Invoice", Map.ofEntries(Map.entry("invoice", invoice)));
            return invoice;
        } else {
            audit.log(EventData.IMPORT, EventData.Status.WARNING, "Invalid Invoice", Map.ofEntries(Map.entry("invoice", source)));
            return null;
        }
    }
}
