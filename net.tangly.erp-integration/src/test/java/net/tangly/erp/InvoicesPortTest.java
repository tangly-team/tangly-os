/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.erp;

import com.google.common.jimfs.Jimfs;
import net.tangly.erp.invoices.artifacts.InvoicesUtilities;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.ports.InvoicesHdl;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicesPortTest {
    @Test
    void testInvoiceDocuments() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var port = new InvoicesAdapter(new InvoicesEntities(), store.invoiceReportsRoot());
            var handler = new InvoicesHdl(new InvoicesEntities(), store.invoicesRoot());
            handler.importEntities();
            port.exportInvoiceDocuments(true, true, true, null, null);

            handler.realm().invoices().items().forEach(o -> assertThat(Files.exists(InvoicesUtilities.resolvePath(store.invoicesRoot(), o))).isTrue());
        }
    }

    private String textFromPdf(Path file) throws IOException {
        try (RandomAccessReadBuffer stream = new RandomAccessReadBuffer(Files.newInputStream(file))) {
            var parser = new PDFParser(stream);
            try (PDDocument document = parser.parse()) {
                var pdfStripper = new PDFTextStripper();
                pdfStripper.setStartPage(1);
                return pdfStripper.getText(document);
            }
        }
    }
}
