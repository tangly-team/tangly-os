/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.jimfs.Jimfs;
import net.tangly.core.TagTypeRegistry;
import net.tangly.invoices.ports.InvoicesAdapter;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import net.tangly.invoices.ports.InvoicesUtilities;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InvoicesPortTest {
    @Test
    void testInvoiceDocuments() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            var port = new InvoicesAdapter(new InvoicesEntities(), store.invoiceReportsRoot());
            var handler = new InvoicesHdl(new InvoicesEntities(), store.invoicesRoot());
            handler.importEntities();
            port.exportInvoiceDocuments(false, true);

            handler.realm().invoices().items().forEach(o -> assertThat(Files.exists(InvoicesUtilities.resolvePath(store.invoicesRoot(), o))).isTrue());
        }
    }

    private String textFromPdf(Path file) throws IOException {
        try (RandomAccessBufferedFileInputStream stream = new RandomAccessBufferedFileInputStream(Files.newInputStream(file))) {
            var parser = new PDFParser(stream);
            parser.parse();
            try (COSDocument cosDoc = parser.getDocument()) {
                var pdfStripper = new PDFTextStripper();
                var pdDoc = new PDDocument(cosDoc);
                pdfStripper.setStartPage(1);
                return pdfStripper.getText(pdDoc);
            }
        }
    }
}
