/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.bus.invoices;

import java.math.BigDecimal;

/**
 * The invoice line represents one line or position in an invoice.
 */
public interface InvoiceLine {
    default boolean isRawItem() {
        return true;
    }

    /**
     * Returns the line position in the invoice.
     *
     * @return the position of the invoice line in the invoice.
     */
    int position();

    /**
     * Returns the human readable text documenting the line.
     *
     * @return the text of the line
     */
    String text();

    /**
     * Returns the amount associated with the line.
     *
     * @return the amount of the line
     */
    BigDecimal amount();

    /**
     * Returns the quantity of items associated with invoice line.
     *
     * @return the quantity of items if defined otherwise zero
     */
    BigDecimal quantity();

    BigDecimal unitPrice();
}
