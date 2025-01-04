/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.erp;

import com.google.common.jimfs.Jimfs;
import net.tangly.core.domain.DocumentGenerator;
import net.tangly.core.domain.Port;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesPort;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

import static net.tangly.erp.invoices.ports.InvoicesAdapter.JSON_EXT;
import static org.assertj.core.api.Assertions.assertThat;

class InvoicesPortTest {
    @Test
    void testInvoiceDocuments() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            InvoicesPort port = new InvoicesAdapter(new InvoicesEntities(),
                store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN), store.docsRoot().resolve(InvoicesBoundedDomain.DOMAIN), new Properties());
            port.importEntities(store);
            port.exportInvoiceDocuments(store, false, false, false, true, null, null, Collections.emptyList());

            port.realm().invoices().items()
                .forEach(o -> assertThat(
                    Files.exists(Port.resolvePath(store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN), o.date().getYear(),
                        o.name() + JSON_EXT))).isTrue());
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

    private Properties properties() {
        Properties properties = new Properties();
        properties.setProperty(DocumentGenerator.THEME_PATH_KEY, "/organization/docs");
        properties.setProperty(DocumentGenerator.COPYRIGHT_KEY, "CC BY 4.0");
        properties.setProperty(DocumentGenerator.ORGANIZATION_NAME_KEY, "organization");
        return properties;
    }
}
