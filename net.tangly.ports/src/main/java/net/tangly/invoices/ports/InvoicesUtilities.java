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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tangly.bus.invoices.Invoice;
import org.jetbrains.annotations.NotNull;

public class InvoicesUtilities {
    public static final String INVOICES = "invoices";
    public static final String INVOICE_NAME_PATTERN = "\\d{4}-\\d{4}-.*";
    private static final Pattern invoicePattern = Pattern.compile(INVOICE_NAME_PATTERN);

    private InvoicesUtilities() {
    }

    /**
     * Resolve the path to where an invoice should be located in the file system. The convention is <em>base directory/invoices/year</em>. If folders do not
     * exist they are created.
     *
     * @param directory base directory containing all invoice reports and documents
     * @param invoice   invoice to write
     * @return path to the folder where the invoice should be written
     */
    public static Path resolvePath(@NotNull Path directory, @NotNull Invoice invoice) {
        Matcher matcher = invoicePattern.matcher(invoice.name());
        Path invoicePath = matcher.matches() ? directory.resolve(invoice.name().substring(0, 4)) : directory;
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
