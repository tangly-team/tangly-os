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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * The subtotal defines the sum for a set of positions in the invoice. A subtotal groups a set of related positions, such as belonging to the
 * same accounting position or project booking, or to the same amount of VAT percentage for mixed VAT invoices.
 */
public record Subtotal(int position, String text, List<InvoiceLine>items) implements InvoiceLine {
    @Override
    public BigDecimal amount() {
        return items.stream().map(InvoiceLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal vat() {
        return items.stream().map(InvoiceLine::vat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal quantity() {
        return items.stream().map(InvoiceLine::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal unitPrice() {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    /**
     * Returns the list of positions part of the subtotal aggregate.
     * @return positions associated with the subtotal
     */
    public List<InvoiceLine> positions() {
        return Collections.unmodifiableList(items);
    }
}
