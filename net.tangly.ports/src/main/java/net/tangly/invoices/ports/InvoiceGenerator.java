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

import java.nio.file.Path;
import java.util.Map;

import net.tangly.bus.invoices.Invoice;
import org.jetbrains.annotations.NotNull;

/**
 * The invoice generator abstraction generates a output document based on the provided invoice and optional configuration properties.
 */
public interface InvoiceGenerator {
    /**
     * Create a new invoice output document.
     *
     * @param invoice     invoice used to create a new invoice document
     * @param invoicePath path to the document to create
     * @param properties  properties to configure the creation process
     */
    void create(@NotNull Invoice invoice, @NotNull Path invoicePath, @NotNull Map<String, Object> properties);
}
