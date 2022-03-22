/*
 * Copyright 2006-2022 Marcel Baumann
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Inject;

import net.tangly.commons.logger.EventData;
import net.tangly.erp.invoices.artifacts.InvoiceJson;
import net.tangly.erp.invoices.artifacts.InvoicesUtilities;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.ArticleCode;
import net.tangly.erp.invoices.services.InvoicesHandler;
import net.tangly.erp.invoices.services.InvoicesRealm;
import net.tangly.erp.ports.TsvHdl;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import static net.tangly.erp.ports.TsvHdl.ID;
import static net.tangly.erp.ports.TsvHdl.MODULE;
import static net.tangly.erp.ports.TsvHdl.NAME;
import static net.tangly.erp.ports.TsvHdl.TEXT;
import static net.tangly.erp.ports.TsvHdl.get;

/**
 * Defines the workflows defined for bounded domain activities in particular the import and export to files.
 */
public class InvoicesHdl implements InvoicesHandler {
    public static final String REPORTS = "reports";
    public static final String ARTICLES_TSV = "articles.tsv";
    public static final String JSON_EXT = ".json";
    private final InvoicesRealm realm;

    /**
     * Path to the root folder of all invoices and product description. Invoices should be grouped by year.
     */
    private final Path invoicesFolder;

    @Inject
    public InvoicesHdl(@NotNull InvoicesRealm realm, @NotNull Path invoicesFolder) {
        this.realm = realm;
        this.invoicesFolder = invoicesFolder;
    }

    @Override
    public InvoicesRealm realm() {
        return realm;
    }

    @Override
    public void importEntities() {
        var invoiceJson = new InvoiceJson(realm);
        importArticles(invoicesFolder.resolve(ARTICLES_TSV));
        try (Stream<Path> stream = Files.walk(invoicesFolder)) {
            AtomicInteger nrOfInvoices = new AtomicInteger();
            AtomicInteger nrOfImportedInvoices = new AtomicInteger();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JSON_EXT)).forEach(o -> {
                nrOfInvoices.getAndIncrement();
                var invoice = invoiceJson.imports(o, Collections.emptyMap());
                if ((invoice != null) && invoice.check()) {
                    nrOfImportedInvoices.getAndIncrement();
                    realm.invoices().update(invoice);
                    EventData.log(EventData.IMPORT, MODULE, EventData.Status.SUCCESS, "Imported Invoice", Map.of("invoice", invoice));
                } else {
                    EventData.log(EventData.IMPORT, MODULE, EventData.Status.WARNING, "Invalid Invoice", Map.of("invoice", o.toString()));
                }
            });
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.INFO, "Invoices were imported out of",
                Map.of("nrOfImportedInvoices", Integer.toString(nrOfImportedInvoices.get()), "nrOfInvoices", Integer.toString(nrOfInvoices.get())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportEntities() {
        exportArticles(invoicesFolder.resolve(ARTICLES_TSV));
        var invoiceJson = new InvoiceJson(realm);
        realm.invoices().items().forEach(o -> {
            var invoiceFolder = InvoicesUtilities.resolvePath(invoicesFolder, o);
            var invoicePath = invoiceFolder.resolve(o.name() + JSON_EXT);
            invoiceJson.exports(o, invoicePath, Collections.emptyMap());
            EventData.log(EventData.EXPORT, MODULE, EventData.Status.SUCCESS, "Invoice exported to JSON {}", Map.of("invoice", o, "invoicePath", invoicePath));
        });
    }

    public void exportArticles(@NotNull Path path) {
        TsvHdl.exportEntities(path, createTsvArticle(), realm.articles());
    }

    public void importArticles(@NotNull Path path) {
        TsvHdl.importEntities(path, createTsvArticle(), realm.articles());
    }

    static TsvEntity<Article> createTsvArticle() {
        Function<CSVRecord, Article> imports = (CSVRecord record) -> new Article(get(record, ID), get(record, NAME), get(record, TEXT),
            Enum.valueOf(ArticleCode.class, get(record, "code").toLowerCase()), TsvProperty.CONVERT_BIG_DECIMAL_FROM.apply(get(record, "unitPrice")),
            get(record, "unit"), TsvProperty.CONVERT_BIG_DECIMAL_FROM.apply(get(record, "vatRate")));

        List<TsvProperty<Article, ?>> fields = List.of(TsvProperty.ofString(ID, Article::id, null), TsvProperty.ofString(NAME, Article::name, null),
            TsvProperty.ofString(TEXT, Article::text, null), TsvProperty.ofEnum(ArticleCode.class, "code", Article::code, null),
            TsvProperty.ofBigDecimal("unitPrice", Article::unitPrice, null), TsvProperty.ofString("unit", Article::unit, null),
            TsvProperty.ofBigDecimal("vatRate", Article::vatRate, null));
        return TsvEntity.of(Article.class, fields, imports);
    }
}
