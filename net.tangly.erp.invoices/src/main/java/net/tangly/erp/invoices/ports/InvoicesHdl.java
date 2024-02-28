/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.invoices.ports;

import net.tangly.commons.logger.EventData;
import net.tangly.core.domain.Handler;
import net.tangly.erp.invoices.artifacts.InvoiceJson;
import net.tangly.erp.invoices.artifacts.InvoicesUtilities;
import net.tangly.erp.invoices.services.InvoicesHandler;
import net.tangly.erp.invoices.services.InvoicesRealm;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.tangly.erp.ports.TsvHdl.MODULE;

/**
 * Define the workflow defined for bounded domain activities in particular the import and export of files.
 */
public class InvoicesHdl implements InvoicesHandler {
    public static final String REPORTS = "reports";
    public static final String ARTICLES_TSV = "articles.tsv";
    public static final String JSON_EXT = ".json";
    private final InvoicesRealm realm;

    /**
     * Path to the root folder of all invoices and product description. Invoices should be grouped by year.
     */
    private final Path folder;

    public InvoicesHdl(@NotNull InvoicesRealm realm, @NotNull Path invoicesFolder) {
        this.realm = realm;
        this.folder = invoicesFolder;
    }

    @Override
    public InvoicesRealm realm() {
        return realm;
    }

    @Override
    public void importEntities() {
        var handler = new InvoicesTsvJsonHdl(realm());
        Handler.importEntities(folder, ARTICLES_TSV, handler::importArticles);
        var invoiceJson = new InvoiceJson(realm);
        try (Stream<Path> stream = Files.walk(folder)) {
            AtomicInteger nrOfInvoices = new AtomicInteger();
            AtomicInteger nrOfImportedInvoices = new AtomicInteger();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JSON_EXT)).forEach(o -> {
                nrOfInvoices.getAndIncrement();
                try (Reader reader = Files.newBufferedReader(folder.resolve(o))) {
                    var invoice = handler.importInvoice(reader, o.toString());
                    if ((invoice != null)) {
                        nrOfImportedInvoices.getAndIncrement();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
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
        var handler = new InvoicesTsvJsonHdl(realm());
        handler.exportArticles(folder.resolve(ARTICLES_TSV));
        var invoiceJson = new InvoiceJson(realm);
        realm.invoices().items().forEach(o -> {
            var invoiceFolder = InvoicesUtilities.resolvePath(folder, o);
            var invoicePath = invoiceFolder.resolve(o.name() + JSON_EXT);
            invoiceJson.exports(o, invoicePath, Collections.emptyMap());
            EventData.log(EventData.EXPORT, MODULE, EventData.Status.SUCCESS, "Invoice exported to JSON {}", Map.of("invoice", o, "invoicePath", invoicePath));
        });
    }

    @Override
    public void clearEntities() {
        realm().invoices().deleteAll();
        realm().articles().deleteAll();
    }
}
