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

package net.tangly.erp.invoices.domain;

import net.tangly.core.HasText;

import java.math.BigDecimal;

/**
 * The invoice line represents one line or position in an invoice. A line should contain only one kind of sold items so that the quantity and unit price are
 * clearly defined.
 */
public sealed interface InvoiceLine extends HasText permits InvoiceItem, Subtotal {
    /**
     * Return the line position in the invoice and is used to order the lines on the invoice output.
     *
     * @return the position of the invoice line in the invoice
     */
    int position();

    /**
     * Return the human-readable text documenting the line.
     *
     * @return the text of the line
     */
    String text();

    /**
     * Return the amount associated with the line.
     *
     * @return the amount of the line
     */
    BigDecimal amount();

    BigDecimal vatRate();
    /**
     * Return the VAT amount associated with the line.
     *
     * @return the VAT amount of the line
     */
    BigDecimal vat();

    /**
     * Return the quantity of items associated with an invoice line.
     *
     * @return the quantity of items if defined otherwise zero
     */
    BigDecimal quantity();

    /**
     * Return the unit price of items associated with an invoice line.
     *
     * @return the unit price of the items for the line
     */
    BigDecimal unitPrice();

    /**
     * Return true if the line contains items.
     *
     * @return flag indicating the line contains items
     */
    boolean isItem();

    /**
     * Return true if the line is an aggregate of other lines.
     *
     * @return flag indicating the line is an aggregate
     */
    default boolean isAggregate() {
        return !isItem();
    }
}
