/*
 *
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.invoices;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * The abstraction defines a subtotal for a set of position in the invoice. A subtotal groups a set of related positions, such as belonging to the
 * same accounting position or project booking, or to the same amount of VAT percentage for mixed VAT invoices.
 */
public class Subtotal implements InvoiceLine {

    private int position;

    private String text;

    private List<InvoiceLine> items;

    public Subtotal(int position, String text, List<InvoiceLine> items) {
        this.position = position;
        this.text = text;
        this.items = List.copyOf(items);
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public BigDecimal amount() {
        return items.stream().map(InvoiceLine::amount).reduce(BigDecimal.ZERO, (a, b) -> (a.add(b)));
    }

    @Override
    public BigDecimal quantity() {
        return items.stream().map(InvoiceLine::quantity).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    }

    @Override
    public BigDecimal unitPrice() {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isRawItem() {
        return false;
    }

    public List<InvoiceLine> positions() {
        return Collections.unmodifiableList(items);
    }
}
