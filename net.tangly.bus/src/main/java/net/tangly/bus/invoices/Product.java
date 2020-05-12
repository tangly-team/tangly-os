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

public class Product {
    private final String productId;

    private final String description;

    private final BigDecimal unitPrice;

    public Product(String productId, String description, BigDecimal unitPrice) {
        this.productId = productId;
        this.description = description;
        this.unitPrice = unitPrice;
    }

    public String productId() {
        return productId;
    }

    public String description() {
        return description;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }
}
