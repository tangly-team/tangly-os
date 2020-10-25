/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.invoices.ports;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;

import net.tangly.bus.invoices.Article;
import net.tangly.bus.invoices.ArticleCode;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import net.tangly.ports.TsvHdl;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import static net.tangly.ports.TsvHdl.ID;
import static net.tangly.ports.TsvHdl.MODULE;
import static net.tangly.ports.TsvHdl.NAME;
import static net.tangly.ports.TsvHdl.TEXT;
import static net.tangly.ports.TsvHdl.get;

/**
 * Defines the workflows defined for bounded domain activities in particular the import and export to files.
 */
public class InvoicesHdl {
    public static final String INVOICES = "invoices";
    public static final String REPORTS = "reports";
    public static final String ARTICLES_TSV = "articles.tsv";
    public static final String JSON_EXT = ".json";
    public static final String INVOICE_NAME_PATTERN = "\\d{4}-\\d{4}-.*";
    private final RealmInvoices realm;
    private final Pattern invoicePattern;

    @Inject
    public InvoicesHdl(RealmInvoices realm) {
        this.realm = realm;
        invoicePattern = Pattern.compile(INVOICE_NAME_PATTERN);
    }

    public RealmInvoices realm() {
        return realm;
    }

    /**
     * Import all invoices to the file system. All invoices are imported from directory/INVOICES. If the name of the invoice starts with a four digits pattern,
     * it is assumed that it represents the year when the invoice was issued.
     *
     * @param directory directory in which the invoices will be written
     * @see #exportEntities(Path) (Path)
     */
    public void importEntities(@NotNull Path directory) {
        InvoiceJson invoiceJson = new InvoiceJson(realm);
        importArticles(directory.resolve(ARTICLES_TSV));
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JSON_EXT)).forEach(o -> {
                Invoice invoice = invoiceJson.imports(o, Collections.emptyMap());
                if (invoice.isValid()) {
                    realm.invoices().update(invoice);
                    EventData.log(EventData.IMPORT, MODULE, EventData.Status.SUCCESS, "Imported Invoice {}", Map.of("invoice", invoice));
                } else {
                    EventData.log(EventData.IMPORT, MODULE, EventData.Status.WARNING, "Invalid Invoice {}", Map.of("invoice", invoice));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Export all invoices to the file system. All invoices are created under directory/INVOICES. If the name of the invoice starts with a four digits pattern,
     * it is assumed that it represents the year when the invoice was issued. A folder with the year will be created and the invoice will be written within.
     *
     * @param directory directory in which the invoices will be written
     * @see #importEntities(Path) (Path)
     */
    public void exportEntities(@NotNull Path directory) {
        exportArticles(directory.resolve(ARTICLES_TSV));
        InvoiceJson invoiceJson = new InvoiceJson(realm);
        realm.invoices().items().forEach(o -> {
            Path invoiceFolder = resolvePath(directory, o);
            Path invoicePath = invoiceFolder.resolve(o.name() + JSON_EXT);
            invoiceJson.exports(o, invoicePath, Collections.emptyMap());
            EventData.log(EventData.EXPORT, "net.tangly.crm.ports", EventData.Status.SUCCESS, "Invoice exported to JSON {}",
                    Map.of("invoice", o, "invoicePath", invoicePath));
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

    public void exportInvoiceToPdf(@NotNull Path directory, @NotNull Invoice invoice) {
        exportInvoiceDocument(invoice, resolvePath(directory.resolve(REPORTS), invoice), true, true);
    }

    /**
     * Export an invoice to a file.
     *
     * @param invoice       invoice to be exported
     * @param invoiceFolder path of the file where the invoice will exported
     * @param withQrCode    flag if the Swiss QR cde should be added to the invoice document
     * @param withEN16931   flag if the EN16931 digital invoice should be added to the invoice document
     */
    public void exportInvoiceDocument(@NotNull Invoice invoice, @NotNull Path invoiceFolder, boolean withQrCode, boolean withEN16931) {
        InvoiceAsciiDoc asciiDocGenerator = new InvoiceAsciiDoc(invoice.locale());
        asciiDocGenerator.exports(invoice, invoiceFolder, Collections.emptyMap());
        Path invoicePath = invoiceFolder.resolve(invoice.name() + AsciiDoctorHelper.PDF_EXT);
        if (withQrCode) {
            InvoiceQrCode qrGenerator = new InvoiceQrCode();
            qrGenerator.exports(invoice, invoicePath, Collections.emptyMap());
        }
        if (withEN16931) {
            InvoiceZugFerd en164391Generator = new InvoiceZugFerd();
            en164391Generator.exports(invoice, invoicePath, Collections.emptyMap());
        }
    }

    /**
     * Resolve the path to where an invoice should be located in the file system. The convention is <i>base directory/invoices/year</i>. If folders do not exist
     * they are created.
     *
     * @param directory base directory containing the invoices
     * @param invoice   invoice to write
     * @return path to the folder where the invoice should be written
     */
    public Path resolvePath(@NotNull Path directory, @NotNull Invoice invoice) {
        Path invoicesPath = directory.resolve(INVOICES);
        Matcher matcher = invoicePattern.matcher(invoice.name());
        Path invoicePath = matcher.matches() ? invoicesPath.resolve(invoice.name().substring(0, 4)) : invoicesPath;
        if (Files.notExists(invoicePath)) {
            try {
                Files.createDirectories(invoicePath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return invoicePath;
    }
}
