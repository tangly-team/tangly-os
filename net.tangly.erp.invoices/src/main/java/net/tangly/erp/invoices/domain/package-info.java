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

/**
 * The package provides core abstractions used in any commercial application in need of invoice handling. The defined information forms the basis to create an
 * output invoice legible for human beings and fulfilling the European requirements for a legal invoice document.
 * <h2>Abstractions</h2>
 * <p>The invoices package is designed as a bounded domain.</p>
 * <p>Articles define the items being sold and are referred in invoice lines.</p>
 * <h2>Invoices</h2>
 * <p>An invoice instance shall fully define the content of legally binding invoice. The provided information allows the sending of an invoice either per
 * post mail or per electronic mail. Digital features such as SwissQR code or European FacturX digital invoice can be created with the invoice data.</p>
 * <p>Invoice instances are archived as a JSON document. Such a document can be stored on a file system or in a database.</p>
 */
package net.tangly.erp.invoices.domain;
