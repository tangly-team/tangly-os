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

package net.tangly.bus.invoices;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the export port for the invoices bounded domain.
 */
public interface InvoicesPort {
    /**
     * Export the document form of the invoice.
     *
     * @param invoice     invoice to export=
     * @param withQrCode  flag indicating if a Swiss QR code payment slip should be generated
     * @param withEN16931 flag indicating if digital invoice data based on standard EN 16931 should be generated
     */
    void exportInvoiceDocument(@NotNull Invoice invoice, boolean withQrCode, boolean withEN16931);
}
