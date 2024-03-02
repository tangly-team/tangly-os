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


import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;


/**
 * The invoice item represents a position of sold items defined through a product.
 * @param position position of the item in the invoice
 * @param article product sold in the invoice line
 * @param text human-readable text documenting the line
 * @param quantity quantity of items sold
 * @param vatRate VAT rate applied to the item
 */
public record InvoiceItem(int position, @NotNull Article article, String text, @NotNull BigDecimal quantity, BigDecimal vatRate) implements InvoiceLine {
    @Override
    public BigDecimal amount() {
        return unitPrice().multiply(quantity);
    }

    @Override
    public BigDecimal vat() {
        return amount().multiply(vatRate());
    }

    @Override
    public BigDecimal unitPrice() {
        return article.unitPrice();
    }

    @Override
    public boolean isItem() {
        return true;
    }
}
