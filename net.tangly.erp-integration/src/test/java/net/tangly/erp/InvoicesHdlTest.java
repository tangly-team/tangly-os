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

package net.tangly.erp;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.jimfs.Jimfs;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.invoices.BusinessLogicInvoices;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import net.tangly.invoices.ports.InvoicesPort;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicesHdlTest {
    public static final String CONTRACT_HSLU_2015 = "HSLU-2015";

    @Test
    @Tag("localTest")
    void testCompanyTsvInvoices() {
        InvoicesHdl handler = new InvoicesHdl(new InvoicesEntities(new TagTypeRegistry()), Path.of("/Users/Shared/tangly/", "invoices"));
        handler.importEntities();
    }

    @Test
    void testTsvInvoices() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            ErpStore store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            InvoicesHdl handler = new InvoicesHdl(new InvoicesEntities(new TagTypeRegistry()), store.invoicesRoot());
            handler.importEntities();

            verifyArticles(handler.realm());
            verifyInvoices(handler.realm());

            verifyBusinessLogic(handler.realm());

            handler.exportEntities();

            handler = new InvoicesHdl(new InvoicesEntities(new TagTypeRegistry()), store.invoicesRoot());
            handler.importEntities();
            verifyArticles(handler.realm());
        }
    }

    @Test
    void testInvoiceDocuments() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            ErpStore store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            InvoicesPort port = new InvoicesPort(new InvoicesEntities(new TagTypeRegistry()), store.invoiceReportsRoot());

            InvoicesHdl handler = new InvoicesHdl(new InvoicesEntities(new TagTypeRegistry()), store.invoicesRoot());
            handler.importEntities();
            port.exportInvoiceDocuments(store.invoiceReportsRoot(), false, false);

            handler.realm().invoices().items().forEach(o -> assertThat(Files.exists(handler.resolvePath(store.invoicesRoot(), o))).isTrue());
        }
    }

    private void verifyInvoices(@NotNull RealmInvoices realm) {
        assertThat(realm.invoices().items().isEmpty()).isFalse();
        realm.invoices().items().forEach(o -> assertThat(o.isValid()).isTrue());
    }

    private void verifyArticles(@NotNull RealmInvoices realm) {
        assertThat(realm.articles().items().isEmpty()).isFalse();
    }

    private void verifyBusinessLogic(@NotNull RealmInvoices realm) {
        BusinessLogicInvoices logic = new BusinessLogicInvoices(realm);
        assertThat(logic.invoicedAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null))
                .isEqualByComparingTo(logic.paidAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null));
        assertThat(logic.expensesForContract(CONTRACT_HSLU_2015, null, null)).isNotNegative();
    }

    private String textFromPdf(Path file) throws IOException {
        try (RandomAccessBufferedFileInputStream stream = new RandomAccessBufferedFileInputStream(Files.newInputStream(file))) {
            PDFParser parser = new PDFParser(stream);
            parser.parse();
            try (COSDocument cosDoc = parser.getDocument()) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                PDDocument pdDoc = new PDDocument(cosDoc);
                pdfStripper.setStartPage(1);
                String parsedText = pdfStripper.getText(pdDoc);
                return parsedText;
            }
        }
    }
}
