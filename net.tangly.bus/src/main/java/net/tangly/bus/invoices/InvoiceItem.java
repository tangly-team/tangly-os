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

public class InvoiceItem implements InvoiceLine {

    private int position;

    private String productId;

    private String text;

    private BigDecimal unitPrice;

    private BigDecimal quantity;


    public InvoiceItem() {
    }

    public InvoiceItem(int position, Product product, BigDecimal quantity) {
        this.position = position;
        this.productId = product.productId();
        this.unitPrice = product.unitPrice();
        this.text = product.description();
        this.quantity = quantity;
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
        return unitPrice.multiply(quantity);
    }

    @Override
    public BigDecimal quantity() {
        return quantity;
    }

    @Override
    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public void position(int position) {
        this.position = position;
    }

    public String productId() {
        return productId;
    }

    public void productId(String productId) {
        this.productId = productId;
    }

    public void text(String text) {
        this.text = text;
    }


    public void unitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }


    public void quantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
