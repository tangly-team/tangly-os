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
 * <h3> Definition</h3>
 * <p>An invoice, bill or tab is a commercial document issued by a seller to a buyer, relating to a sale transaction and indicating the products, quantities,
 * and agreed prices for products or services the seller had provided the buyer.</p>
 * <p>Payment terms are usually stated on the invoice. These may specify that the buyer has a maximum number of days in which to pay and is sometimes offered a
 * discount if paid before the due date. The buyer could have already paid for the products or services listed on the invoice. To avoid confusion, and
 * consequent unnecessary communications from buyer to seller, some sellers clearly state in large and/or capital letters on an invoice whether it has already
 * been paid.</p>
 * <p> From the point of view of a seller, an invoice is a sales invoice. From the point of view of a buyer, an invoice is a purchase invoice. The document
 * indicates the buyer and seller, but the term invoice indicates money is owed or owing.</p>
 * <p>Within the European union, an invoice is primarily legally defined by the EU VAT directive as an accounting voucher (to verify tax and VAT reporting) and
 * secondly as a Civil law (common law) document.</p>
 * <h3>Implementation</h3>
 * <p>An invoice instance shall fully define the content of legally binding invoice. The provided information allows the sending of an invoice either per
 * post mail or per electronic mail. Digital features such as SwissQR code or European FacturX digital invoice can be created with the invoice data.</p>
 * <p>Invoice instances are archived as a JSON document. Such a document can be stored on a file system or in a database.</p>
 */
package net.tangly.erp.invoices.domain;
